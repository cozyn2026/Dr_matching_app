import { useState } from 'react';
import { request } from './api.js';

const initialForm = {
  organizationName: '',
  displayName: '',
  email: '',
  password: '',
  passwordConfirmation: '',
  currentPosition: '',
  careerSummary: '',
  researchSummary: '',
  preferredDegreeField: '',
  preferredStartTerm: '',
  preferredStudyMode: '',
  preferredRegion: '',
  workBalanceConditions: '',
  privacyConsent: false,
};

export default function ResearcherRegistration() {
  const [form, setForm] = useState(initialForm);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [submitted, setSubmitted] = useState(false);

  function changeField(event) {
    const { name, value, checked, type } = event.target;
    setForm((current) => ({ ...current, [name]: type === 'checkbox' ? checked : value }));
  }

  async function submit(event) {
    event.preventDefault();
    if (form.password !== form.passwordConfirmation) {
      setError('パスワードと確認入力が一致しません。');
      return;
    }
    setSaving(true);
    setError('');
    try {
      const { passwordConfirmation, ...registration } = form;
      await request('/api/researcher-registrations', {
        method: 'POST',
        body: JSON.stringify(registration),
      });
      setSubmitted(true);
      setForm(initialForm);
    } catch (err) {
      setError(err.message || '通信に失敗しました。時間をおいて再度お試しください。');
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="panel registration-panel">
      <div className="section-heading">
        <div><div className="eyebrow">RESEARCHER APPLICATION</div><h2>企業研究者として登録</h2></div>
        <span className="step-badge">会員申請</span>
      </div>
      <p className="registration-intro">研究テーマや進学希望を登録すると、会員確認の申請として保存されます。申請中のプロフィールは非公開です。</p>
      {error && <div className="message error" role="alert">{error}</div>}
      {submitted && <div className="message success" role="status">研究者登録を受け付けました。会員確認後に利用を開始できます。</div>}
      <form onSubmit={submit}>
        <div className="registration-grid">
          <label>氏名<input name="displayName" value={form.displayName} onChange={changeField} maxLength="120" autoComplete="name" required /></label>
          <label>メールアドレス<input name="email" type="email" value={form.email} onChange={changeField} maxLength="254" autoComplete="email" required /></label>
          <label>ログイン用パスワード<input name="password" type="password" value={form.password} onChange={changeField} minLength="12" maxLength="72" autoComplete="new-password" required /><small>12文字以上72文字以内。承認後のログインに使います。</small></label>
          <label>パスワード（確認）<input name="passwordConfirmation" type="password" value={form.passwordConfirmation} onChange={changeField} minLength="12" maxLength="72" autoComplete="new-password" required /></label>
          <label className="wide">所属企業名<input name="organizationName" value={form.organizationName} onChange={changeField} maxLength="200" autoComplete="organization" required /></label>
          <label className="wide">現在の役職<input name="currentPosition" value={form.currentPosition} onChange={changeField} maxLength="160" required /></label>
          <label className="wide">職歴・研究経験<textarea name="careerSummary" value={form.careerSummary} onChange={changeField} maxLength="3000" rows="3" required /></label>
          <label className="wide">取り組みたい研究テーマ<textarea name="researchSummary" value={form.researchSummary} onChange={changeField} maxLength="3000" rows="4" required /></label>
          <label>希望する博士課程の分野<input name="preferredDegreeField" value={form.preferredDegreeField} onChange={changeField} maxLength="160" placeholder="例：材料科学・電気化学" required /></label>
          <label>進学希望時期<input name="preferredStartTerm" value={form.preferredStartTerm} onChange={changeField} maxLength="40" placeholder="例：2028年度" required /></label>
          <label>希望する学び方<input name="preferredStudyMode" value={form.preferredStudyMode} onChange={changeField} maxLength="120" placeholder="例：長期履修、週末中心" required /></label>
          <label>希望地域<input name="preferredRegion" value={form.preferredRegion} onChange={changeField} maxLength="120" placeholder="例：関東、オンライン併用" required /></label>
          <label className="wide">仕事との両立で希望する条件<textarea name="workBalanceConditions" value={form.workBalanceConditions} onChange={changeField} maxLength="3000" rows="3" required /></label>
        </div>
        <label className="checkbox-label registration-consent">
          <input type="checkbox" name="privacyConsent" checked={form.privacyConsent} onChange={changeField} required />
          入力情報が会員申請の確認に必要な範囲で利用されることに同意します。
        </label>
        <div className="form-actions">
          <button className="button primary" type="submit" disabled={saving}>{saving ? '送信中…' : '登録申請を送信'}</button>
        </div>
      </form>
    </section>
  );
}
