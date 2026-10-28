package jp.drmatching.laboratory;

public class LaboratoryNotFoundException extends RuntimeException {
    public LaboratoryNotFoundException(Long id) {
        super("研究室 ID " + id + " が見つかりません。");
    }
}
