package jp.drmatching.admin;

public class RegistrationReviewNotFoundException extends RuntimeException {
    public RegistrationReviewNotFoundException(Long membershipId) {
        super("承認待ちの申請が見つかりません（ID: " + membershipId + "）。");
    }
}
