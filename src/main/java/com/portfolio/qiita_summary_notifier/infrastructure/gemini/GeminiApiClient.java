package com.portfolio.qiita_summary_notifier.infrastructure.gemini;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.genai.Client;
import com.google.genai.gaos.models.interactions.Content;
import com.google.genai.gaos.models.interactions.CreateModelInteraction;
import com.google.genai.gaos.models.interactions.CreateModelInteractionResponseFormat;
import com.google.genai.gaos.models.interactions.Interaction;
import com.google.genai.gaos.models.interactions.InteractionsInput;
import com.google.genai.gaos.models.interactions.Model;
import com.google.genai.gaos.models.interactions.ModelOutputStep;
import com.google.genai.gaos.models.interactions.ResponseFormat;
import com.google.genai.gaos.models.interactions.Step;
import com.google.genai.gaos.models.interactions.TextContent;
import com.google.genai.gaos.models.interactions.TextResponseFormat;
import com.google.genai.gaos.models.interactions.TextResponseFormatMimeType;
import com.google.genai.gaos.models.operations.CreateInteractionRequestBody;
import com.portfolio.qiita_summary_notifier.infrastructure.gemini.dto.GeminiResponseDto;
import com.portfolio.qiita_summary_notifier.infrastructure.gemini.exception.GeminiSummaryFailedException;
import com.portfolio.qiita_summary_notifier.service.Article;
import com.portfolio.qiita_summary_notifier.service.Summarizer;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j 
@Component
public class GeminiApiClient implements Summarizer{
    
    private final Client client;
    private final ObjectMapper objectMapper;

    private static final String GEMINI_MODEL = "gemini-3.5-flash-lite";

    public GeminiApiClient(Client client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    @Override
    public GeminiResponseDto fetchSummaryOfArticle(Article article) {
        // スキーマの定義からプロンプトまで作成
        CreateModelInteraction params = createPrompt(article.getTitle(), article.getBody());

        try {
            // リクエスト送信とレスポンス内容を格納
            Interaction interaction = 
                client.interactions.create(CreateInteractionRequestBody.of(params)).interaction().get();

            log.debug("interactionの中身: {}", interaction);
    
            // ネストされている要約部分を取り出す処理
            StringBuilder output = new StringBuilder();
            for (Step step : interaction.steps().orElse(List.of())) {
                if (step instanceof ModelOutputStep modelOutputStep) {
                    for (Content content : modelOutputStep.content().orElse(List.of())) {
                        if (content instanceof TextContent textContent) {
                            textContent.text().ifPresent(output::append);
                        }
                    }
                }
            }

            return objectMapper.readValue(output.toString(), GeminiResponseDto.class);

        } catch (RuntimeException e) {
            log.error("geminiAPIエラー", e);
            throw new GeminiSummaryFailedException(
                "記事ID " + article.getId() + " の要約取得に失敗しました", e);
        }
    }

    private CreateModelInteraction createPrompt(String title, String body) {
        // Json Schemaを作成
        Map<String, Object> summaryJsonSchema = createSummaryJsonSchema();
        CreateModelInteractionResponseFormat format = 
            CreateModelInteractionResponseFormat.of(
                ResponseFormat.of(
                    TextResponseFormat.builder()
                            .mimeType(TextResponseFormatMimeType.APPLICATION_JSON)
                            .schema(summaryJsonSchema)
                            .build()
                )
            );

        String prompt = """
        あなたは優秀な技術記事のキュレーターです。
        与えられた記事の内容を、Discordで共有するための「3行サマリー」に要約してください。
        出力は必ず以下の3行のみとし、それぞれの行は指定された役割を厳守してください。

        【出力フォーマットと各行の役割】
        1行目（テーマと技術）: 記事の主題と、扱っている具体的な技術名やツール名を明記する。
        2行目（記事の魅力）: 読者がこの記事を読むことで得られる具体的なメリットや、実践的な学びの内容を記載する。
        3行目（対象読者）: 「〇〇に悩んでいる人」「〇〇をこれから始める開発者」など、抱えている課題や具体的な状況でターゲットを表現する。

        【出力例】
        1行目: ReactとTypeScriptを用いた、コンポーネント設計と状態管理のベストプラクティスについての記事です。
        2行目: 再利用性の高いコンポーネントの分割手法や、パフォーマンス低下を防ぐ具体的な実装パターンをコード付きで学べます。
        3行目: プロジェクトの規模が大きくなり、Propsのバケツリレーやレンダリングの最適化に課題を感じているフロントエンドエンジニア向け。

        【タイトル】
        %s
        【本文】
        %s
        """.formatted(title, body);
        
        CreateModelInteraction params = 
            CreateModelInteraction.builder()
                    .model(Model.of(GEMINI_MODEL))
                    .input(InteractionsInput.of(prompt))
                    .responseFormat(format)
                    .build();

        return params;
    }

    // Geminiに出力してほしいJSON この形を定義するためのメソッド
    // {
    //     "theme": "何について書かれた内容か",
    //     "key_takeaways": "記事の重要な部分",
    //     "target_audience": "対象の読者"
    // }
    private Map<String, Object> createSummaryJsonSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "theme", Map.of(
                    "type", "string",
                    "description", "記事のテーマと使われている技術を一文で記述してください。"
                ),
                "key_takeaways", Map.of(
                    "type", "string",
                    "description", "記事の魅力と読むことで得られるメリットを一文で記述してください。"
                ),
                "target_audience", Map.of(
                    "type", "string",
                    "description", "記事が想定している読者層を一文で記述してください。"
                )
            ),
            "required", List.of("theme", "key_takeaways", "target_audience")
        );
    }
}
