import { useEffect, useState } from 'react';

const emptyForm = {
  universityName: '',
  graduateSchoolName: '',
  laboratoryName: '',
  supervisorName: '',
  researchField: '',
  researchSummary: '',
  acceptsWorkingProfessionals: false,
};

async function request(path, options = {}) {
  const response = await fetch(path, {
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  });

  if (!response.ok) {
    const problem = await response.json().catch(() => ({}));
    throw new Error(problem.message || `通信に失敗しました（${response.status}）`);
  }

  return response.status === 204 ? null : response.json();
}

export default function App() {
  const [laboratories, setLaboratories] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  async function loadLaboratories() {
    setLoading(true);
    setError('');
    try {
      setLaboratories(await request('/api/laboratories'));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadLaboratories();
  }, []);

  function changeField(event) {
    const { name, value, checked, type } = event.target;
    setForm((current) => ({ ...current, [name]: type === 'checkbox' ? checked : value }));
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
      const url = editingId ? `/api/laboratories/${editingId}` : '/api/laboratories';
      await request(url, {
        method: editingId ? 'PUT' : 'POST',
        body: JSON.stringify(form),
      });
      setNotice(editingId ? '研究室情報を更新しました。' : '研究室を登録しました。');
      resetForm();
      await loadLaboratories();
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
      await request(`/api/laboratories/${laboratory.id}`, { method: 'DELETE' });
      setNotice('研究室を削除しました。');
      if (editingId === laboratory.id) resetForm();
      await loadLaboratories();
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <a className="brand" href="#top" aria-label="博士進学マッチ ホーム">
          <span className="brand-mark">博</span>
          <span><strong>博士進学マッチ</strong><small>研究室カタログ</small></span>
        </a>
        <span className="topbar-note">研究テーマから、次の一歩を。</span>
      </header>

      <main id="top" className="page">
        <section className="intro">
          <div className="eyebrow">LABORATORY DIRECTORY</div>
          <h1>大学院研究室を<br className="mobile-break" />探す・登録する</h1>
          <p>研究室の情報を登録し、一覧で確認できます。登録した情報はAPI経由でデータベースに保存されます。</p>
          <div className="stack-note"><span /> React <b>→</b> Spring Boot API <b>→</b> PostgreSQL</div>
        </section>

        {error && <div className="message error" role="alert">{error}</div>}
        {notice && <div className="message success" role="status">{notice}</div>}

        <section className="panel form-panel">
          <div className="section-heading">
            <div><div className="eyebrow">{editingId ? 'EDIT' : 'NEW ENTRY'}</div><h2>{editingId ? '研究室情報を編集' : '研究室を登録'}</h2></div>
            <span className="step-badge">{editingId ? '更新' : '登録'}</span>
          </div>
          <form onSubmit={submit}>
            <div className="form-grid">
              <label>大学名<input name="universityName" value={form.universityName} onChange={changeField} maxLength="120" required placeholder="例：東都大学" /></label>
              <label>研究科名<input name="graduateSchoolName" value={form.graduateSchoolName} onChange={changeField} maxLength="160" required placeholder="例：工学研究科" /></label>
              <label>研究室名<input name="laboratoryName" value={form.laboratoryName} onChange={changeField} maxLength="160" required placeholder="例：材料設計研究室" /></label>
              <label>指導教員<input name="supervisorName" value={form.supervisorName} onChange={changeField} maxLength="120" required placeholder="例：山田 花子" /></label>
              <label className="wide">研究分野<input name="researchField" value={form.researchField} onChange={changeField} maxLength="100" required placeholder="例：材料工学、ロボティクス" /></label>
              <label className="wide">研究内容<textarea name="researchSummary" value={form.researchSummary} onChange={changeField} maxLength="3000" required rows="3" placeholder="研究テーマや研究室の紹介を入力してください" /></label>
            </div>
            <label className="checkbox-label"><input type="checkbox" name="acceptsWorkingProfessionals" checked={form.acceptsWorkingProfessionals} onChange={changeField} />社会人学生の受入れを相談できる</label>
            <div className="form-actions">
              {editingId && <button className="button secondary" type="button" onClick={resetForm}>編集をやめる</button>}
              <button className="button primary" type="submit" disabled={saving}>{saving ? '保存中…' : editingId ? '変更を保存' : '研究室を登録'}</button>
            </div>
          </form>
        </section>

        <section className="catalog-section">
          <div className="section-heading catalog-heading">
            <div><div className="eyebrow">CATALOG</div><h2>登録済みの研究室</h2></div>
            <span className="count">{laboratories.length} 件</span>
          </div>
          {loading ? <div className="empty-state">研究室を読み込んでいます…</div> : laboratories.length === 0 ? (
            <div className="empty-state"><span className="empty-icon">＋</span><strong>研究室はまだ登録されていません</strong><p>上のフォームから最初の研究室を登録してください。</p></div>
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
                    <button className="button secondary small" onClick={() => startEdit(laboratory)}>編集</button>
                    <button className="button danger small" onClick={() => deleteLaboratory(laboratory)}>削除</button>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>
        <footer>博士進学マッチ <span>研究室情報はこのアプリの開発用データベースに保存されます。</span></footer>
      </main>
    </div>
  );
}
