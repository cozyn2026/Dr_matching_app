import { useState } from 'react';
import { request } from './api.js';

export default function ResearcherLogin({ onLogin, accountRole = 'researcher' }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  async function submit(event) {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      const session = await request('/api/auth/login', {
        method: 'POST', body: JSON.stringify({ email, password }),
      });
      if (session.role !== accountRole) {
        await request('/api/auth/logout', { method: 'POST' });
        throw new Error(accountRole === 'university_staff'
          ? '大学担当者アカウントでログインしてください。'
          : '研究者アカウントでログインしてください。');
      }
      onLogin(session);
    } catch (err) {
      setError(err.message || 'ログインできませんでした。');
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="panel login-panel">
      <div className="section-heading">
        <div><div className="eyebrow">{accountRole === 'university_staff' ? 'UNIVERSITY LOGIN' : 'RESEARCHER LOGIN'}</div><h2>{accountRole === 'university_staff' ? '大学担当者のログイン' : '承認済み研究者のログイン'}</h2></div>
        <span className="step-badge">会員限定</span>
      </div>
      <p className="registration-intro">{accountRole === 'university_staff' ? '大学担当者アカウントのメールアドレスとパスワードでログインしてください。' : '会員申請が承認された方は、登録したメールアドレスとパスワードでログインしてください。'}</p>
      {error && <div className="message error" role="alert">{error}</div>}
      <form onSubmit={submit} className="login-form">
        <label>メールアドレス<input type="email" autoComplete="username" value={email} onChange={(event) => setEmail(event.target.value)} required /></label>
        <label>パスワード<input type="password" autoComplete="current-password" value={password} onChange={(event) => setPassword(event.target.value)} required /></label>
        <div className="form-actions"><button className="button primary" type="submit" disabled={saving}>{saving ? 'ログイン中…' : 'ログイン'}</button></div>
      </form>
    </section>
  );
}
