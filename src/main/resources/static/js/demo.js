// htmlが読み込まれた後に実行されるようにする
document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('trialBtn');
    
    // ボタンのクリックイベントをここで紐付け
    btn.addEventListener('click', async () => {
        const includeVal = document.getElementById('include').value;
        const urlVal = document.getElementById('webhookUrl').value;
        const resultArea = document.getElementById('sendResult');

        // --- バリデーション（入力チェック） ---
        
        // ① URLの空チェックと形式チェック
        if (!urlVal.startsWith('https://discord.com/api/webhooks/')) {
            resultArea.textContent = "正しいDiscordのWebhook URLを入力してください。";
            resultArea.style.color = "red";
            return; // ここで処理を止める（送信しない）
        }

        // ② 使ってほしくない記号のチェック（例: < > & ' " を禁止）
        // /[<>&'"]/ は「これらの記号のどれかが含まれているか」を判定する記号（正規表現）です
        const invalidSymbolPattern = /[<>&'"]/;
        if (invalidSymbolPattern.test(includeVal)) {
            resultArea.textContent = "キーワードに不正な記号（< > & ' \"）は使用できません。";
            resultArea.style.color = "red";
            return; 
        }

        // --- ここから下で、fetchを使ったAPI送信処理を書く ---
        resultArea.textContent = "送信中...";
        resultArea.style.color = "black";
        
    });
});