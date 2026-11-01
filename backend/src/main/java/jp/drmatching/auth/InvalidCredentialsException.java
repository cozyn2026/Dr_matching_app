package jp.drmatching.auth;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("メールアドレス、パスワード、または会員承認状態を確認してください。");
    }
}
