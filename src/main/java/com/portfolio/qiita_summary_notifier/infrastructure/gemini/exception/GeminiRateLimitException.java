// package com.portfolio.qiita_summary_notifier.infrastructure.gemini.exception;

// /**memo: カスタム例外
//  * geminiの例外クラスで、429だけリトライさせたかったのでこのクラスに変換する
//  * 429はtoo many requests  
//  * 無料枠は制限が厳しいので引っかかる可能性あり
//  */
// public class GeminiRateLimitException extends RuntimeException{

//     public GeminiRateLimitException(String message, Throwable cause) {
//         super(message, cause);
//     }
// }
