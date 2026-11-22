package com.portfolio.qiita_summary_notifier.infrastructure.qiita;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import com.portfolio.qiita_summary_notifier.infrastructure.qiita.dto.QiitaArticleDto;
import com.portfolio.qiita_summary_notifier.service.Article;
import com.portfolio.qiita_summary_notifier.service.ArticleProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Component 
public class QiitaApiClient implements ArticleProvider{

    private final RetryTemplate retryTemplate;
    private final RestClient restClient;

    private static final String QIITA_API_URL = "https://qiita.com/api/v2/items";
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PER_PAGE = 3;

    public QiitaApiClient(
        @Qualifier("qiitaRestClient") RestClient restClient, 
        @Qualifier("qiitaRetryTemplate") RetryTemplate retryTemplate
    ) {
        this.restClient = restClient;
        this.retryTemplate = retryTemplate;
    }

    @Override 
    public List<Article> fetchArticles(String includeTags, String excludeTags) {
        
        URI uri = createUri(includeTags, excludeTags);      
        List<Article> articleList = new ArrayList<>();
        try {
            retryTemplate.invoke(() -> {
                QiitaArticleDto[] dtos = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(QiitaArticleDto[].class);

                if (dtos != null) {
                    for (QiitaArticleDto dto : dtos) {
                        articleList.add(convertToArticle(dto));
                    }
                }

                log.info("取得した記事数: {}件", articleList.size());   
            });

            return articleList;
            
        } catch(RestClientException e) {
            log.error("QiitaAPIエラー", e);
            return new ArrayList<>();
        } 
    }

    private URI createUri(String includeTags, String excludeTags) {
        // 除外(exclude)タグのクエリを作成する
        // 入出力例: "ポエム, , MCP" -> -tag:ポエム -tag:MCP
        List<String> excludeKeywords = Arrays.asList(excludeTags.split(","));
        String excludeQuery = excludeKeywords.stream()
                .map(s -> s.trim())                             
                .filter(s -> !s.isEmpty())                      
                .map(s -> "-tag:" + s)            
                .collect(Collectors.joining(" "));

        log.debug("excludeQueryの内容: {}", excludeQuery);

        // 包含(include)タグを追加し、検索クエリを作成する
        // 入出力例: "Java, spring" -> "tag:Java -tag:ポエム -tag:MCP OR tag:spring -tag:ポエム -tag:MCP"
        List<String> includeKeywords = Arrays.asList(includeTags.split(","));
        String searchQuery = includeKeywords.stream()
                .map(s -> s.trim())
                .filter(s -> !s.isEmpty())
                .map(s -> "tag:" + s + " " + excludeQuery)  // ここで-tagをつなげる
                .collect(Collectors.joining(" OR "));

        log.debug("searchQueryの内容: {}", searchQuery);

        // 想定されるuri: "https://qiita.com/api/v2/items?query=" + searchQuery + " sort:created&page=1&per_page=3";
        return UriComponentsBuilder.fromUriString(QIITA_API_URL)
                .queryParam("query", searchQuery + " sort:created")
                .queryParam("page", DEFAULT_PAGE)
                .queryParam("per_page", DEFAULT_PER_PAGE)
                .encode()   // uriのエンコードを指定 デフォルトでUTF-8
                .build()    
                .toUri();   
    }

    private Article convertToArticle(QiitaArticleDto dto) {
        return new Article(
            dto.getTitle(),
            dto.getUrl(),
            dto.getBody(),
            dto.getId(),
            dto.getUser(),
            dto.getTags()
        );
    }
}
