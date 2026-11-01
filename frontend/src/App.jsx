import { useEffect, useState } from 'react';
import { request } from './api.js';
import ResearcherRegistration from './ResearcherRegistration.jsx';
import ResearcherRegistrationAdmin from './ResearcherRegistrationAdmin.jsx';
import ResearcherLogin from './ResearcherLogin.jsx';

const emptyForm = {
  universityName: '',
  graduateSchoolName: '',
  laboratoryName: '',
  supervisorName: '',
  researchField: '',
  researchSummary: '',
  acceptsWorkingProfessionals: false,
};

export default function App() {
  const [laboratories, setLaboratories] = useState([]);
  const [universityLaboratories, setUniversityLaboratories] = useState([]);
  const [interestedResearchers, setInterestedResearchers] = useState([]);
  const [universityNotifications, setUniversityNotifications] = useState([]);
  const [universityLoading, setUniversityLoading] = useState(false);
  const [favoriteIds, setFavoriteIds] = useState(() => new Set());
  const [favoriteBusyId, setFavoriteBusyId] = useState(null);
  const [keyword, setKeyword] = useState('');
  const [activeKeyword, setActiveKeyword] = useState('');
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [view, setView] = useState('catalog');
  const [currentResearcher, setCurrentResearcher] = useState(null);
  const [authLoading, setAuthLoading] = useState(true);

  async function loadLaboratories(searchTerm = activeKeyword) {
    setLoading(true);
    setError('');
    try {
      const query = searchTerm.trim() ? `?keyword=${encodeURIComponent(searchTerm.trim())}` : '';
      setLaboratories(await request(`/api/laboratories${query}`));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    request('/api/auth/me')
      .then(setCurrentResearcher)
      .catch(() => setCurrentResearcher(null))
      .finally(() => setAuthLoading(false));
  }, []);

  useEffect(() => {
    if (currentResearcher?.role === 'researcher') {
      loadLaboratories('');
      request('/api/favorites')
        .then((ids) => setFavoriteIds(new Set(ids)))
        .catch((err) => setError(err.message));
    } else if (currentResearcher?.role === 'university_staff') {
      loadUniversityData();
    } else {
      setFavoriteIds(new Set());
    }
  }, [currentResearcher]);

  useEffect(() => {
    if (currentResearcher?.role !== 'university_staff') return undefined;
    const refreshNotifications = () => request('/api/university/notifications')
      .then(setUniversityNotifications)
      .catch(() => {});
    const intervalId = window.setInterval(refreshNotifications, 30000);
    window.addEventListener('focus', refreshNotifications);
    return () => {
      window.clearInterval(intervalId);
      window.removeEventListener('focus', refreshNotifications);
    };
  }, [currentResearcher]);

  async function logout() {
    try {
      await request('/api/auth/logout', { method: 'POST' });
    } catch {
      // Clear local catalog state even when the session has already expired.
    } finally {
      setCurrentResearcher(null);
      setLaboratories([]);
      setFavoriteIds(new Set());
      setUniversityLaboratories([]);
      setInterestedResearchers([]);
    }
  }

  async function searchLaboratories(event) {
    event.preventDefault();
    setActiveKeyword(keyword.trim());
    await loadLaboratories(keyword);
  }

  async function clearSearch() {
    setKeyword('');
    setActiveKeyword('');
    await loadLaboratories('');
  }

  function changeField(event) {
    const { name, value, checked, type } = event.target;
    setForm((current) => ({ ...current, [name]: type === 'checkbox' ? checked : value }));
  }

  async function loadUniversityData() {
    setUniversityLoading(true);
    setError('');
    try {
      const [labs, researchers] = await Promise.all([
        request('/api/university/laboratories'),
        request('/api/university/interested-researchers'),
      ]);
      const notifications = await request('/api/university/notifications');
      setUniversityLaboratories(labs);
      setInterestedResearchers(researchers);
      setUniversityNotifications(notifications);
    } catch (err) {
      setError(err.message);
    } finally {
      setUniversityLoading(false);
    }
  }

  async function openUniversityResearchers() {
    setView('universityResearchers');
    try {
      await request('/api/university/notifications/read', { method: 'PUT' });
      setUniversityNotifications((current) => current.map((notification) => ({ ...notification, read: true })));
    } catch (err) {
      setError(err.message);
    }
  }

  function startEdit(laboratory) {
    setEditingId(laboratory.id);
    setForm({
      universityName: laboratory.universityName,
      graduateSchoolName: laboratory.graduateSchoolName,
      laboratoryName: laboratory.laboratoryName,
      supervisorName: laboratory.supervisorName,
      researchField: laboratory.researchField,
      researchSummary: laboratory.researchSummary,
      acceptsWorkingProfessionals: laboratory.acceptsWorkingProfessionals,
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function resetForm() {
    setEditingId(null);
    setForm(emptyForm);
  }

  async function submit(event) {
    event.preventDefault();
    setSaving(true);
    setError('');
    setNotice('');
    try {
      await request(editingId ? `/api/university/laboratories/${editingId}` : '/api/university/laboratories', {
        method: editingId ? 'PUT' : 'POST',
        body: JSON.stringify({ ...form, universityName: currentResearcher?.organizationName }),
      });
      setNotice(editingId ? '研究室情報を更新しました。' : '研究室を登録しました。');
      resetForm();
      await loadUniversityData();
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  async function deleteLaboratory(laboratory) {
    if (!window.confirm(`「${laboratory.laboratoryName}」を削除しますか？`)) return;
    setError('');
    setNotice('');
    try {
      await request(`/api/university/laboratories/${laboratory.id}`, {
        method: 'DELETE',
      });
      setNotice('研究室を削除しました。');
      if (editingId === laboratory.id) resetForm();
      await loadUniversityData();
    } catch (err) {
      setError(err.message);
    }
  }

  async function toggleFavorite(laboratory) {
    const isFavorite = favoriteIds.has(laboratory.id);
    if (!isFavorite && !window.confirm('お気に入り登録すると、この大学の担当者にあなたのプロフィールと進学希望が共有されます。登録しますか？')) return;
    setFavoriteBusyId(laboratory.id);
    setError('');
    setNotice('');
    try {
      await request(`/api/favorites/${laboratory.id}`, { method: isFavorite ? 'DELETE' : 'PUT' });
      setFavoriteIds((current) => {
        const next = new Set(current);
        if (isFavorite) next.delete(laboratory.id);
        else next.add(laboratory.id);
        return next;
      });
      setNotice(isFavorite ? 'お気に入りを解除しました。' : '研究室をお気に入りに登録しました。');
    } catch (err) {
      setError(err.message);
    } finally {
      setFavoriteBusyId(null);
    }
  }

  const laboratoryRegistrationForm = <section className="panel form-panel">
    <div className="section-heading">
      <div><div className="eyebrow">{editingId ? 'EDIT' : 'NEW ENTRY'}</div><h2>{editingId ? '研究室情報を編集' : '大学研究室の登録'}</h2></div>
      <span className="step-badge">{editingId ? '更新' : '大学向け'}</span>
    </div>
    <p className="registration-intro">登録先：{currentResearcher?.organizationName}</p>
    <form onSubmit={submit}>
      <div className="form-grid">
        <label>大学名<input name="universityName" value={currentResearcher?.organizationName || ''} readOnly required /></label>
        <label>研究科名<input name="graduateSchoolName" value={form.graduateSchoolName} onChange={changeField} maxLength="160" required placeholder="例：工学研究科" /></label>
        <label>研究室名<input name="laboratoryName" value={form.laboratoryName} onChange={changeField} maxLength="160" required placeholder="例：材料設計研究室" /></label>
        <label>指導教員<input name="supervisorName" value={form.supervisorName} onChange={changeField} maxLength="120" required placeholder="例：山田 花子" /></label>
        <label className="wide">研究分野<input name="researchField" value={form.researchField} onChange={changeField} maxLength="100" required placeholder="例：材料工学、ロボティクス" /></label>
        <label className="wide">研究内容<textarea name="researchSummary" value={form.researchSummary} onChange={changeField} maxLength="3000" required rows="3" placeholder="研究テーマや研究室の紹介を入力してください" /></label>
      </div>
      <label className="checkbox-label"><input type="checkbox" name="acceptsWorkingProfessionals" checked={form.acceptsWorkingProfessionals} onChange={changeField} />社会人学生の受入れを相談できる</label>
      <div className="form-actions">{editingId && <button className="button secondary" type="button" onClick={resetForm}>編集をやめる</button>}<button className="button primary" type="submit" disabled={saving}>{saving ? '保存中…' : editingId ? '変更を保存' : '研究室を登録'}</button></div>
    </form>
  </section>;

  const interestedResearchersPanel = <section className="catalog-section">
    <div className="section-heading catalog-heading">
      <div><div className="eyebrow">RESEARCHER INTEREST</div><h2>お気に入り登録した研究者</h2></div>
      <button className="button secondary small" type="button" onClick={loadUniversityData} disabled={universityLoading}>{universityLoading ? '更新中…' : '情報を更新'}</button>
    </div>
    {universityLoading ? <div className="empty-state">研究者情報を読み込んでいます…</div> : interestedResearchers.length === 0 ? <div className="empty-state"><strong>お気に入り登録した研究者はまだいません</strong><p>研究者がお気に入り登録すると、ここにプロフィールと進学希望が表示されます。</p></div> : <div className="laboratory-list">
      {interestedResearchers.map((person) => <article className="laboratory-card" key={`${person.laboratoryId}-${person.displayName}`}>
        <div className="card-top"><span className="field-tag">{person.preferredDegreeField}</span></div><h3>{person.displayName}</h3>
        <p className="school-line">お気に入り研究室：{person.laboratoryName} ・ {person.currentPosition}</p>
        <p className="summary">{person.researchSummary}</p>
        <dl className="admin-details"><dt>職歴・研究経験</dt><dd>{person.careerSummary}</dd><dt>進学希望</dt><dd>{person.preferredStartTerm} ・ {person.preferredStudyMode} ・ {person.preferredRegion}</dd><dt>仕事との両立条件</dt><dd>{person.workBalanceConditions}</dd></dl>
      </article>)}
    </div>}
    <section className="notification-section" aria-label="通知">
      <div className="section-heading catalog-heading"><div><div className="eyebrow">NOTIFICATIONS</div><h2>通知</h2></div><span className="count">未読 {universityNotifications.filter((notification) => !notification.read).length} 件</span></div>
      {universityNotifications.length === 0 ? <p className="notification-empty">研究室のお気に入り登録があると、ここに通知されます。</p> : <ul className="notification-list">
        {universityNotifications.map((notification) => <li className={notification.read ? '' : 'unread'} key={notification.id}>
          <span><strong>{notification.researcherName}</strong> さんが「{notification.laboratoryName}」をお気に入り登録しました。</span>
          <time dateTime={notification.createdAt}>{new Date(notification.createdAt).toLocaleString('ja-JP')}</time>
        </li>)}
      </ul>}
    </section>
  </section>;

  return (
    <div className="app-shell">
      <header className="topbar">
        <a className="brand" href="#top" aria-label="博士進学マッチ ホーム">
          <span className="brand-mark">博</span>
          <span><strong>博士進学マッチ</strong><small>研究室検索・研究者登録</small></span>
        </a>
        <nav className="topbar-nav" aria-label="メインメニュー">
          <button className={view === 'catalog' ? 'active' : ''} type="button" onClick={() => setView('catalog')}>研究室を探す</button>
          {currentResearcher?.role === 'university_staff' ? <>
            <button className={view === 'university' ? 'active' : ''} type="button" onClick={() => setView('university')}>研究室管理</button>
            <button className={view === 'universityResearchers' ? 'active' : ''} type="button" onClick={openUniversityResearchers}>お気に入り研究者{universityNotifications.some((notification) => !notification.read) && <span className="notification-badge">{universityNotifications.filter((notification) => !notification.read).length}</span>}</button>
          </> : <button className={view === 'university' || view === 'universityResearchers' ? 'active' : ''} type="button" onClick={() => setView('university')}>大学担当者ログイン</button>}
          <button className={view === 'registration' ? 'active' : ''} type="button" onClick={() => setView('registration')}>研究者登録</button>
          <button className={view === 'admin' ? 'active' : ''} type="button" onClick={() => setView('admin')}>管理者</button>
          {currentResearcher && <button className="logout-button" type="button" onClick={logout}>ログアウト</button>}
        </nav>
      </header>

      <main id="top" className="page">
        {view === 'catalog' ? (
          authLoading ? <div className="empty-state">ログイン状態を確認しています…</div> : currentResearcher?.role === 'researcher' ? <>
        <section className="intro">
          <div className="eyebrow">LABORATORY DIRECTORY</div>
          <h1>大学院研究室を<br className="mobile-break" />探す</h1>
          <p>研究テーマから、自分に合う研究室を探してお気に入りに保存できます。</p>
        </section>

        {error && <div className="message error" role="alert">{error}</div>}
        {notice && <div className="message success" role="status">{notice}</div>}
        <p className="registration-intro">お気に入り登録すると、この大学の担当者にプロフィールと進学希望が共有されます。解除すると以後閲覧できません。</p>

        <section className="catalog-section">
          <div className="section-heading catalog-heading">
            <div><div className="eyebrow">CATALOG</div><h2>登録済みの研究室</h2></div>
            <span className="count">{laboratories.length} 件</span>
          </div>
          <form className="search-form" onSubmit={searchLaboratories} role="search">
            <label htmlFor="laboratory-search">キーワード</label>
            <input id="laboratory-search" type="search" value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="大学名、研究室名、研究分野など" />
            <button className="button primary" type="submit" disabled={loading}>検索</button>
            {activeKeyword && <button className="button secondary" type="button" onClick={clearSearch} disabled={loading}>解除</button>}
          </form>
          {loading ? <div className="empty-state">研究室を読み込んでいます…</div> : laboratories.length === 0 ? (
            <div className="empty-state"><span className="empty-icon">{activeKeyword ? '⌕' : '＋'}</span><strong>{activeKeyword ? '一致する研究室はありません' : '研究室はまだ登録されていません'}</strong><p>{activeKeyword ? '別のキーワードで検索してください。' : '大学研究室登録メニューから登録できます。'}</p></div>
          ) : (
            <div className="laboratory-list">
              {laboratories.map((laboratory) => (
                <article className="laboratory-card" key={laboratory.id}>
                  <div className="card-top"><span className="field-tag">{laboratory.researchField}</span>{laboratory.acceptsWorkingProfessionals && <span className="working-tag">社会人受入れ相談可</span>}</div>
                  <h3>{laboratory.laboratoryName}</h3>
                  <p className="school-line">{laboratory.universityName} ・ {laboratory.graduateSchoolName}</p>
                  <p className="supervisor">指導教員：{laboratory.supervisorName}</p>
                  <p className="summary">{laboratory.researchSummary}</p>
                  <div className="card-actions">
                    <button className={`button small favorite-button${favoriteIds.has(laboratory.id) ? ' selected' : ''}`} type="button" aria-pressed={favoriteIds.has(laboratory.id)} disabled={favoriteBusyId === laboratory.id} onClick={() => toggleFavorite(laboratory)}>{favoriteBusyId === laboratory.id ? '更新中…' : favoriteIds.has(laboratory.id) ? '♥ お気に入り済み' : '♡ お気に入り'}</button>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>
        </> : currentResearcher ? <div className="empty-state"><strong>研究室検索には研究者アカウントが必要です</strong><button className="button secondary" type="button" onClick={() => setView('university')}>大学担当者メニューへ</button></div> : <ResearcherLogin onLogin={setCurrentResearcher} />
        ) : view === 'university' ? (
          authLoading ? <div className="empty-state">ログイン状態を確認しています…</div> : currentResearcher?.role === 'university_staff' ? <>
          <section className="intro">
            <div className="eyebrow">UNIVERSITY LABORATORY ENTRY</div>
            <h1>大学研究室登録</h1>
            <p>{currentResearcher.organizationName} の研究室情報を登録・管理し、お気に入り登録した研究者の情報を確認できます。</p>
            <div className="stack-note"><span /> 大学担当者ログイン中</div>
          </section>
          {error && <div className="message error" role="alert">{error}</div>}
          {notice && <div className="message success" role="status">{notice}</div>}
          {laboratoryRegistrationForm}
          <section className="catalog-section">
            <div className="section-heading catalog-heading"><div><div className="eyebrow">LABORATORY MANAGEMENT</div><h2>登録済み研究室</h2></div><span className="count">{universityLaboratories.length} 件</span></div>
            {universityLoading ? <div className="empty-state">大学情報を読み込んでいます…</div> : universityLaboratories.length === 0 ? <div className="empty-state"><strong>登録済みの研究室はありません</strong></div> : <div className="laboratory-list">
              {universityLaboratories.map((laboratory) => <article className="laboratory-card" key={laboratory.id}>
                <div className="card-top"><span className="field-tag">{laboratory.researchField}</span></div><h3>{laboratory.laboratoryName}</h3><p className="school-line">{laboratory.universityName} ・ {laboratory.graduateSchoolName}</p>
                <div className="card-actions"><button className="button secondary small" type="button" onClick={() => startEdit(laboratory)}>編集</button><button className="button danger small" type="button" onClick={() => deleteLaboratory(laboratory)}>削除</button></div>
              </article>)}
            </div>}
          </section>
          <section className="catalog-section"><button className="button secondary" type="button" onClick={openUniversityResearchers}>お気に入り登録した研究者を見る（{interestedResearchers.length}人）</button></section>
        </> : currentResearcher ? <div className="empty-state"><strong>大学担当者アカウントに切り替えてください</strong><button className="button secondary" type="button" onClick={logout}>ログアウトして切り替え</button></div> : <ResearcherLogin accountRole="university_staff" onLogin={setCurrentResearcher} />
        ) : view === 'universityResearchers' ? (
          authLoading ? <div className="empty-state">ログイン状態を確認しています…</div> : currentResearcher?.role === 'university_staff' ? <>
          <section className="intro">
            <div className="eyebrow">RESEARCHER INTEREST</div>
            <h1>お気に入り登録した研究者</h1>
            <p>{currentResearcher.organizationName} の研究室をお気に入り登録した承認済み研究者のプロフィールと進学希望を確認できます。</p>
            <div className="stack-note"><span /> 大学担当者ログイン中</div>
          </section>
          {error && <div className="message error" role="alert">{error}</div>}
          {interestedResearchersPanel}
        </> : currentResearcher ? <div className="empty-state"><strong>大学担当者アカウントに切り替えてください</strong><button className="button secondary" type="button" onClick={logout}>ログアウトして切り替え</button></div> : <ResearcherLogin accountRole="university_staff" onLogin={setCurrentResearcher} />
        ) : view === 'registration' ? (
          <>
          <section className="intro">
            <div className="eyebrow">DOCTORAL STUDY APPLICATION</div>
            <h1>研究者登録</h1>
            <p>企業で働きながら博士号取得を目指す方の会員申請を受け付けます。</p>
            <div className="stack-note"><span /> 申請情報は確認完了まで非公開です</div>
          </section>
          <ResearcherRegistration />
          </>
        ) : (
          <>
          <section className="intro">
            <div className="eyebrow">ADMINISTRATOR</div>
            <h1>会員申請を確認</h1>
            <p>研究者の会員申請を確認し、承認または却下します。</p>
          </section>
          <ResearcherRegistrationAdmin />
          </>
        )}
        <footer>博士進学マッチ <span>研究室情報はこのアプリの開発用データベースに保存されます。</span></footer>
      </main>
    </div>
  );
}
