import { useState } from 'react';
import { request } from './api.js';

export default function ResearcherRegistrationAdmin() {
  const [adminKey, setAdminKey] = useState('');
  const [registrations, setRegistrations] = useState([]);
  const [loaded, setLoaded] = useState(false);
  const [busyId, setBusyId] = useState(null);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  async function loadRegistrations(event) {
    event?.preventDefault();
    setError('');
    setNotice('');
    try {
      setRegistrations(await request('/api/admin/researcher-registrations', {
        headers: { 'X-Admin-Key': adminKey },
      }));
      setLoaded(true);
    } catch (err) {
      setError(err.message || '通信に失敗しました。');
      setLoaded(false);
    }
  }

  async function review(registration, decision) {
    const action = decision === 'approved' ? '承認' : '却下';
    if (!window.confirm(`${registration.displayName} さんの申請を${action}しますか？`)) return;
    setBusyId(registration.membershipId);
    setError('');
    setNotice('');
    try {
      const result = await request(`/api/admin/researcher-registrations/${registration.membershipId}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json', 'X-Admin-Key': adminKey },
        body: JSON.stringify({ decision }),
      });
      await loadRegistrations();
      setNotice(result.message);
    } catch (err) {
      setError(err.message || '通信に失敗しました。');
    } finally {
      setBusyId(null);
    }
  }

  function renderRegistration(registration) {
    const isPending = registration.membershipStatus === 'pending';
    const statusLabel = {
      pending: '承認待ち',
      rejected: '却下',
      active: '承認済み',
    }[registration.membershipStatus] || registration.membershipStatus;

    return (
      <article className="admin-registration-card" key={registration.membershipId}>
        <div className="admin-registration-heading">
          <div><span className="field-tag">{registration.preferredDegreeField}</span><h3>{registration.displayName}</h3></div>
          <span className={`step-badge review-status ${registration.membershipStatus}`}>{statusLabel}</span>
        </div>
        <p className="school-line">{registration.organizationName} ・ {registration.currentPosition}</p>
        <p className="admin-email">{registration.email}</p>
        <dl className="admin-details">
          <dt>職歴・研究経験</dt><dd>{registration.careerSummary}</dd>
          <dt>希望する研究</dt><dd>{registration.researchSummary}</dd>
          <dt>進学希望</dt><dd>{registration.preferredStartTerm} ・ {registration.preferredStudyMode} ・ {registration.preferredRegion}</dd>
          <dt>仕事との両立条件</dt><dd>{registration.workBalanceConditions}</dd>
        </dl>
        {isPending && <div className="card-actions">
          <button className="button danger small" type="button" disabled={busyId === registration.membershipId} onClick={() => review(registration, 'rejected')}>却下</button>
          <button className="button primary small" type="button" disabled={busyId === registration.membershipId} onClick={() => review(registration, 'approved')}>{busyId === registration.membershipId ? '処理中…' : '承認'}</button>
        </div>}
      </article>
    );
  }

  const unapproved = registrations.filter((registration) => registration.membershipStatus !== 'active');
  const approved = registrations.filter((registration) => registration.membershipStatus === 'active');

  return (
    <section className="panel registration-panel admin-panel">
      <div className="section-heading">
        <div><div className="eyebrow">ADMIN REVIEW</div><h2>研究者登録の承認</h2></div>
        <span className="step-badge">管理者</span>
      </div>
      <p className="registration-intro">開発環境用の管理キーで未承認・承認済みの申請を確認します。キーはこの画面を閉じると破棄されます。</p>
      <form className="admin-key-form" onSubmit={loadRegistrations}>
        <label htmlFor="admin-key">管理キー</label>
        <input id="admin-key" type="password" value={adminKey} onChange={(event) => setAdminKey(event.target.value)} autoComplete="off" required />
        <button className="button primary" type="submit">申請を読み込む</button>
      </form>
      {error && <div className="message error" role="alert">{error}</div>}
      {notice && <div className="message success" role="status">{notice}</div>}
      {loaded && <div className="admin-review-sections">
        <section className="admin-review-group" aria-labelledby="unapproved-heading">
          <div className="admin-group-heading"><h3 id="unapproved-heading">未承認</h3><span className="count">{unapproved.length} 件</span></div>
          {unapproved.length ? <div className="admin-registration-list">{unapproved.map(renderRegistration)}</div> : <div className="empty-state admin-empty"><strong>未承認の申請はありません</strong></div>}
        </section>
        <section className="admin-review-group" aria-labelledby="approved-heading">
          <div className="admin-group-heading"><h3 id="approved-heading">承認済み</h3><span className="count">{approved.length} 件</span></div>
          {approved.length ? <div className="admin-registration-list">{approved.map(renderRegistration)}</div> : <div className="empty-state admin-empty"><strong>承認済みの申請はありません</strong></div>}
        </section>
      </div>}
    </section>
  );
}
