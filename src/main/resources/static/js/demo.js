// htmlが読み込まれた後に実行されるようにする
document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('demoBtn');
    
    // ボタンのクリックイベントをここで紐付け
    btn.addEventListener('click', async () => {
        const includeVal = document.getElementById('include').value;
        const excludeVal = document.getElementById('exclude').value;
        const urlVal = document.getElementById('webhookUrl').value;
        const resultArea = document.getElementById('result');

        // --- バリデーション（入力チェック） ---
        
        // 未入力チェック
        if (!includeVal) {
            resultArea.textContent = "検索タグは必須です";
            resultArea.style.color = "red";
            return;
        }

        // ① URLの空チェックと形式チェック
        // 先頭の文字列が引数とマッチしているか
        if (!urlVal.startsWith('https://discord.com/api/webhooks/')) {
            resultArea.textContent = "正しいDiscordのWebhook URLを入力してください。";
            resultArea.style.color = "red";
            return; // ここで処理を止める（送信しない）
        }

        // ② 使ってほしくない記号のチェック（例: < > & ' " を禁止）
        // /[<>&'"]/ は「これらの記号のどれかが含まれているか」を判定する記号（正規表現）です
        const invalidSymbolPattern = /[<>&'"]/;
        if (invalidSymbolPattern.test(includeVal) || invalidSymbolPattern.test(excludeVal)) {
            resultArea.textContent = "キーワードに不正な記号（< > & ' \"）は使用できません。";
            resultArea.style.color = "red";
            return; 
        }

        // ボタンを押した後は処理が完了するまで押せなくする
        btn.disabled = true;

        // --- ここから下で、fetchを使ったAPI送信処理を書く ---
        resultArea.textContent = "送信中...";
        resultArea.style.color = "black";
        
        /**memo: やりたいこと
         * ここで必要な情報を入れてjsonを作成
         * それを非同期(await)でバックのjavaapiを呼び出す
         * 
         * jsonの作り方を調べる
         * => 連想配列を作ってみる
         * 
         * 必要な情報を整理する
         * apiの呼び出し方を調べる
         */

        const requestData = {
            id: 1,
            includeTags: includeVal,
            excludeTags: excludeVal,
            webhookUrl: urlVal
        }

        try {
            /**memo: jsのpostリクエスト
             * 第一引数にuri, 第二引数に連想配列(json)でリクエストを送っている
             */
            const response = await fetch('/api/notifications/demo', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestData)
            });

            if (!response.ok) {
                throw new Error('通知に失敗しました');
            }

            const result = await response.json();
            if (result.success) {
                resultArea.innerText = result.message;
                resultArea.style.color = 'green';
            } else {
                resultArea.innerText = result.message;
                resultArea.style.color = 'red'; 
            }

        } catch (error) {
            console.error('取得失敗', error);
            resultArea.innerText = '通信に失敗しました';
            resultArea.style.color = 'red';
        } finally {
            btn.disabled = false;
        }
        
        
    });
});