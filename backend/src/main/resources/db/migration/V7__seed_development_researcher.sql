-- ローカル開発専用の承認済みテスト研究者。パスワードは LabMatch-Demo-2026!。
WITH demo AS (
    SELECT
        '博士進学マッチ 開発用テスト企業'::VARCHAR AS organization_name,
        '開発用テスト研究者'::VARCHAR AS display_name,
        'demo.researcher@example.com'::VARCHAR AS email,
        '研究開発部 テスト研究員'::VARCHAR AS current_position,
        'ローカル画面確認用の架空プロフィール。実在の研究者・企業とは関係ありません。'::TEXT AS career_summary,
        '研究室検索、お気に入り登録、ログイン後の画面確認に利用するためのテストアカウントです。'::TEXT AS research_summary,
        '情報科学・材料科学'::VARCHAR AS preferred_degree_field,
        '2027年度'::VARCHAR AS preferred_start_term,
        '長期履修・社会人特別選抜を希望'::VARCHAR AS preferred_study_mode,
        '全国・オンライン併用'::VARCHAR AS preferred_region,
        '開発用の架空データのみを使用します。'::TEXT AS work_balance_conditions
), inserted_organization AS (
    INSERT INTO organizations (organization_type, name, membership_status, approved_at)
    SELECT 'company', organization_name, 'active', CURRENT_TIMESTAMP FROM demo
    ON CONFLICT (organization_type, name) DO UPDATE
        SET membership_status = 'active', approved_at = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP
    RETURNING id
), inserted_account AS (
    INSERT INTO accounts (email, password_hash, display_name, account_status, email_verified_at)
    SELECT 'demo.researcher@example.com',
           '$2a$12$VQnVu/JuXbxOytx67WsEze6f0hpDSUOkK9wM2USv3cyBQJ2tCqKgK',
           display_name, 'active', CURRENT_TIMESTAMP
    FROM demo
    ON CONFLICT (email) DO UPDATE
        SET password_hash = EXCLUDED.password_hash, display_name = EXCLUDED.display_name,
            account_status = 'active', email_verified_at = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP
    RETURNING id
), inserted_membership AS (
    INSERT INTO memberships (account_id, organization_id, role, membership_status, approved_at)
    SELECT inserted_account.id, inserted_organization.id, 'researcher', 'active', CURRENT_TIMESTAMP
    FROM inserted_account CROSS JOIN inserted_organization
    ON CONFLICT (account_id, organization_id, role) DO UPDATE
        SET membership_status = 'active', approved_at = CURRENT_TIMESTAMP
    RETURNING id, account_id
), inserted_profile AS (
    INSERT INTO researcher_profiles (account_id, current_position, career_summary, research_summary)
    SELECT inserted_account.id, demo.current_position, demo.career_summary, demo.research_summary
    FROM inserted_account CROSS JOIN demo
    ON CONFLICT (account_id) DO UPDATE
        SET current_position = EXCLUDED.current_position,
            career_summary = EXCLUDED.career_summary,
            research_summary = EXCLUDED.research_summary,
            updated_at = CURRENT_TIMESTAMP
    RETURNING id
), inserted_preference AS (
    INSERT INTO doctorate_preferences (
        researcher_profile_id, preferred_degree_field, preferred_start_term,
        preferred_study_mode, preferred_region, work_balance_conditions
    )
    SELECT inserted_profile.id, demo.preferred_degree_field, demo.preferred_start_term,
           demo.preferred_study_mode, demo.preferred_region, demo.work_balance_conditions
    FROM inserted_profile CROSS JOIN demo
    ON CONFLICT (researcher_profile_id) DO UPDATE
        SET preferred_degree_field = EXCLUDED.preferred_degree_field,
            preferred_start_term = EXCLUDED.preferred_start_term,
            preferred_study_mode = EXCLUDED.preferred_study_mode,
            preferred_region = EXCLUDED.preferred_region,
            work_balance_conditions = EXCLUDED.work_balance_conditions
    RETURNING researcher_profile_id
), inserted_consent AS (
    INSERT INTO consent_records (account_id, consent_type, policy_version, scope)
    SELECT inserted_account.id, 'researcher_registration', 'development-v1', '開発用テストアカウント'
    FROM inserted_account
    WHERE NOT EXISTS (
        SELECT 1 FROM consent_records c
        WHERE c.account_id = inserted_account.id AND c.consent_type = 'researcher_registration'
    )
    RETURNING id
)
INSERT INTO membership_reviews (membership_id, administrator_id, decision)
SELECT inserted_membership.id, administrators.id, 'approved'
FROM inserted_membership
CROSS JOIN administrators
WHERE administrators.active = TRUE
ORDER BY administrators.id
LIMIT 1;
