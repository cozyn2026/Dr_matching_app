-- ローカル画面確認用の大学担当者アカウント。実在の大学担当者ではありません。
INSERT INTO organizations (organization_type, name, membership_status, approved_at)
VALUES ('university', '東京大学', 'active', CURRENT_TIMESTAMP)
ON CONFLICT (organization_type, name) DO UPDATE
    SET membership_status = 'active', approved_at = CURRENT_TIMESTAMP,
        updated_at = CURRENT_TIMESTAMP;

INSERT INTO accounts (email, password_hash, display_name, account_status, email_verified_at)
VALUES ('demo.university@example.com',
        '$2a$12$DErTxAelBU.gENw6cfc5cua6LyeqYwH7PZjuYsn42M9eCLktw4RRK',
        '東京大学 開発用担当者', 'active', CURRENT_TIMESTAMP)
ON CONFLICT (email) DO UPDATE
    SET password_hash = EXCLUDED.password_hash, display_name = EXCLUDED.display_name,
        account_status = 'active', email_verified_at = CURRENT_TIMESTAMP,
        updated_at = CURRENT_TIMESTAMP;

INSERT INTO memberships (account_id, organization_id, role, membership_status, approved_at)
SELECT a.id, o.id, 'university_staff', 'active', CURRENT_TIMESTAMP
FROM accounts a
JOIN organizations o ON o.organization_type = 'university' AND o.name = '東京大学'
WHERE lower(a.email) = lower('demo.university@example.com')
ON CONFLICT (account_id, organization_id, role) DO UPDATE
    SET membership_status = 'active', approved_at = CURRENT_TIMESTAMP;
