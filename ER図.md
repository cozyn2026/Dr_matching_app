# 博士進学マッチ ER図

## 1. 対象範囲

初期版で扱う会員企業・大学、利用者、研究者プロフィール、研究室、検索分野、お気に入り、相談、回答・面談候補、同意、管理操作を表す。自動推薦、入試・出願、決済、自由形式チャットのデータは対象外とする。

## 2. ER図

```mermaid
erDiagram
  ORGANIZATION {
    bigint organization_id PK
    string organization_type "company / university"
    string name
    string membership_status "pending / active / suspended"
    datetime approved_at
    datetime created_at
    datetime updated_at
  }

  ACCOUNT {
    bigint account_id PK
    string email UK
    string password_hash
    string display_name
    string account_status "pending / active / suspended"
    datetime email_verified_at
    datetime last_login_at
    datetime created_at
    datetime updated_at
  }

  MEMBERSHIP {
    bigint membership_id PK
    bigint account_id FK
    bigint organization_id FK
    string role "researcher / university_staff / office"
    string membership_status "pending / active / rejected / suspended"
    bigint approved_by_account_id FK
    datetime approved_at
    datetime created_at
  }

  RESEARCHER_PROFILE {
    bigint researcher_profile_id PK
    bigint account_id FK_UK
    string career_summary
    string current_position
    string research_summary
    string profile_visibility "private / by_consultation"
    datetime created_at
    datetime updated_at
  }

  DOCTORATE_PREFERENCE {
    bigint researcher_profile_id PK_FK
    string preferred_degree_field
    string preferred_start_term
    string preferred_study_mode
    string preferred_region
    string work_balance_conditions
    datetime updated_at
  }

  RESEARCH_FIELD {
    bigint research_field_id PK
    string field_name UK
    boolean is_active
  }

  RESEARCHER_FIELD {
    bigint researcher_profile_id PK_FK
    bigint research_field_id PK_FK
    string interest_level
  }

  LABORATORY {
    bigint laboratory_id PK
    bigint university_organization_id FK
    string graduate_school_name
    string laboratory_name
    string supervisor_name
    string research_summary
    string publication_status "draft / pending_review / published / suspended"
    datetime published_at
    datetime created_at
    datetime updated_at
  }

  LABORATORY_MANAGER {
    bigint laboratory_id PK_FK
    bigint membership_id PK_FK
    string manager_role
    datetime assigned_at
  }

  LABORATORY_CONDITION {
    bigint laboratory_id PK_FK
    boolean accepts_working_professionals
    string study_modes
    string region
    string application_period
    string requirements
    string availability_status "open / paused"
    datetime updated_at
  }

  LABORATORY_FIELD {
    bigint laboratory_id PK_FK
    bigint research_field_id PK_FK
  }

  FAVORITE {
    bigint researcher_profile_id PK_FK
    bigint laboratory_id PK_FK
    datetime created_at
  }

  CONSULTATION {
    bigint consultation_id PK
    bigint researcher_profile_id FK
    bigint laboratory_id FK
    string consultation_body
    string contact_preference
    string status "submitted / reviewing / scheduling / completed / declined / cancelled"
    datetime submitted_at
    datetime updated_at
  }

  CONSULTATION_RESPONSE {
    bigint response_id PK
    bigint consultation_id FK
    bigint responder_membership_id FK
    string response_type "accept / decline / information"
    string response_body
    datetime created_at
  }

  MEETING_OPTION {
    bigint meeting_option_id PK
    bigint response_id FK
    datetime candidate_at
    string status "proposed / selected / declined / withdrawn"
    datetime updated_at
  }

  CONSULTATION_EVENT {
    bigint consultation_event_id PK
    bigint consultation_id FK
    bigint actor_membership_id FK
    string event_type
    string from_status
    string to_status
    string note
    datetime created_at
  }

  CONSENT_RECORD {
    bigint consent_record_id PK
    bigint account_id FK
    string consent_type "profile_sharing / terms / privacy"
    string policy_version
    string scope
    datetime consented_at
    datetime revoked_at
  }

  AUDIT_LOG {
    bigint audit_log_id PK
    bigint actor_account_id FK
    string action
    string target_type
    bigint target_id
    string result
    string request_id
    datetime created_at
  }

  INQUIRY {
    bigint inquiry_id PK
    bigint account_id FK
    string subject
    string body
    string status "received / in_progress / answered / closed"
    bigint assigned_to_account_id FK
    datetime created_at
    datetime updated_at
  }

  ORGANIZATION ||--o{ MEMBERSHIP : has
  ACCOUNT ||--o{ MEMBERSHIP : joins
  ACCOUNT ||--o| RESEARCHER_PROFILE : owns
  ACCOUNT ||--o{ CONSENT_RECORD : gives
  ACCOUNT ||--o{ AUDIT_LOG : performs
  ACCOUNT ||--o{ INQUIRY : submits
  ORGANIZATION ||--o{ LABORATORY : owns
  MEMBERSHIP ||--o{ LABORATORY_MANAGER : assigned
  LABORATORY ||--o{ LABORATORY_MANAGER : managed_by
  LABORATORY ||--|| LABORATORY_CONDITION : has
  RESEARCHER_PROFILE ||--o| DOCTORATE_PREFERENCE : specifies
  RESEARCHER_PROFILE ||--o{ RESEARCHER_FIELD : interested_in
  RESEARCH_FIELD ||--o{ RESEARCHER_FIELD : classifies
  LABORATORY ||--o{ LABORATORY_FIELD : categorized_as
  RESEARCH_FIELD ||--o{ LABORATORY_FIELD : classifies
  RESEARCHER_PROFILE ||--o{ FAVORITE : saves
  LABORATORY ||--o{ FAVORITE : saved_as
  RESEARCHER_PROFILE ||--o{ CONSULTATION : submits
  LABORATORY ||--o{ CONSULTATION : receives
  CONSULTATION ||--o{ CONSULTATION_RESPONSE : answered_by
  MEMBERSHIP ||--o{ CONSULTATION_RESPONSE : responds
  CONSULTATION_RESPONSE ||--o{ MEETING_OPTION : proposes
  CONSULTATION ||--o{ CONSULTATION_EVENT : records
  MEMBERSHIP ||--o{ CONSULTATION_EVENT : acts
  ACCOUNT ||--o{ MEMBERSHIP : approves
  ACCOUNT ||--o{ INQUIRY : assigned_to
```

## 3. 主な関係と制約

- 1つの会員組織に複数の利用者が所属できる。利用者の役割は組織との `MEMBERSHIP` で管理し、1人が複数の役割・組織を持てる設計とする。
- 企業・大学の会員組織は `ORGANIZATION.organization_type` で区別する。研究室は大学組織にのみ所属させる。これは外部キーだけでは保証できないため、アプリケーション側でも検証する。
- `RESEARCHER_PROFILE.account_id` は一意とし、研究者アカウントに対しプロフィールを1件だけ登録する。博士取得の希望条件はプロフィールに1件紐づける。
- 研究者と研究室の研究分野は多対多とし、`RESEARCH_FIELD` の管理語彙を検索条件に使う。自由入力キーワードを追加する場合は、別途タグテーブルを設ける。
- お気に入りは `researcher_profile_id + laboratory_id` を複合主キーとし、同じ研究室を重複登録できない。
- 相談は研究者から研究室に対して作成する。担当大学の有効な `LABORATORY_MANAGER` だけが回答・面談候補の提示を行えるよう、APIで所属と権限を検証する。
- 相談状態を変更するたび `CONSULTATION_EVENT` に履歴を追加する。現在状態は `CONSULTATION.status` に保持し、履歴を上書きしない。
- 面談候補は大学側の回答に紐づけて複数登録できる。相談ごとに選択できる候補は最大1件とし、選択日時・選択者を保存する必要がある場合は `MEETING_OPTION` にその項目を追加する。
- プロフィール公開への同意は、同意対象・ポリシー版・共有範囲・同意日時を記録する。同意撤回後は新たな共有を停止する。
- `AUDIT_LOG` には管理操作の記録だけを保存し、パスワード、認証トークン、相談本文、非公開プロフィールを含めない。

## 4. インデックス・一意制約の例

| 対象 | 制約・インデックス案 | 目的 |
|---|---|---|
| `ACCOUNT.email` | UNIQUE（正規化した小文字メールアドレス） | ログインID重複の防止 |
| `MEMBERSHIP` | UNIQUE（account_id, organization_id, role） | 同一組織・同一役割の重複登録防止 |
| `RESEARCHER_FIELD` | PRIMARY KEY（researcher_profile_id, research_field_id） | 分野の重複登録防止 |
| `LABORATORY_FIELD` | PRIMARY KEY（laboratory_id, research_field_id） | 研究室分野の重複登録防止 |
| `FAVORITE` | PRIMARY KEY（researcher_profile_id, laboratory_id） | お気に入り重複防止 |
| `CONSULTATION` | INDEX（researcher_profile_id, submitted_at） | 研究者の相談一覧表示 |
| `CONSULTATION` | INDEX（laboratory_id, status, submitted_at） | 大学担当者の対応一覧表示 |
| `LABORATORY` | INDEX（publication_status, university_organization_id） | 公開中研究室の検索・管理 |
| `CONSULTATION_EVENT` | INDEX（consultation_id, created_at） | 相談履歴の時系列表示 |

## 5. 実装時に追加確認する項目

1. 相談本文、プロフィール、操作履歴の保存期間と退会後の扱い。
2. 大学担当者の研究室への割当方法と、担当者異動時の引継ぎ。
3. 会員組織に複数の管理者を認めるか、役割を複数保持できるか。
4. 面談候補のタイムゾーン、回答期限、候補変更・取消の扱い。
5. 氏名・メールアドレス等の個人情報を、DB暗号化に加えて項目単位でも暗号化するか。
6. 問い合わせ履歴やメール通知送信結果をDBで保持する期間。

このER図は初期案であり、データベース製品や実際の運営規程に合わせて、型、必須制約、削除方針を確定する。
