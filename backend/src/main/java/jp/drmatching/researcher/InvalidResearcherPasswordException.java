package jp.drmatching.researcher;

public class InvalidResearcherPasswordException extends RuntimeException {
    public InvalidResearcherPasswordException() {
        super("パスワードはUTF-8で72バイト以内にしてください。");
    }
}
