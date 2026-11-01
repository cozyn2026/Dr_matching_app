package jp.drmatching.researcher;

public class ResearcherRegistrationConflictException extends RuntimeException {
    public ResearcherRegistrationConflictException() {
        super("このメールアドレスは登録済みです。");
    }
}
