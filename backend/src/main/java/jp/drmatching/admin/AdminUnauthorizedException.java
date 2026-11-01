package jp.drmatching.admin;

public class AdminUnauthorizedException extends RuntimeException {
    public AdminUnauthorizedException() {
        super("管理キーが正しくありません。");
    }
}
