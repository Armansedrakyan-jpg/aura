import { useState } from 'react';

export default function HomeScreen({ error, onAnalyze, onHistory, onLogout }) {
  const [file, setFile] = useState(null);
  const [preview, setPreview] = useState('');
  const [aboutMe, setAboutMe] = useState('');

  const onFileSelected = (e) => {
    const selected = e.target.files[0];
    if (!selected) return;
    setFile(selected);
    setPreview(URL.createObjectURL(selected));
  };

  return (
    <div className="screen">
      <div className="topbar">
        <div className="logo-small">
          <span className="logo-dot" /> Aura
        </div>
        <div className="actions">
          <button type="button" className="btn-link" onClick={onHistory}>
            История
          </button>
          <button type="button" className="btn-link" onClick={onLogout}>
            Выйти
          </button>
        </div>
      </div>

      <label className="dropzone" htmlFor="photo-input">
        {preview && <img src={preview} alt="Выбранное фото" />}
        {!preview && <span className="hint">Нажми, чтобы выбрать фото</span>}
        <input id="photo-input" type="file" accept="image/*" onChange={onFileSelected} />
      </label>

      <div className="field">
        <label htmlFor="about-me">Расскажи о себе — это тоже влияет на результат</label>
        <textarea
          id="about-me"
          placeholder="Пара слов о себе — характер, интересы, стиль жизни..."
          value={aboutMe}
          onChange={(e) => setAboutMe(e.target.value)}
        />
      </div>

      {error && <div className="error-msg">{error}</div>}

      <button
        className="btn-primary"
        type="button"
        disabled={!file}
        onClick={() => onAnalyze(file, aboutMe)}
      >
        Узнать свою ауру
      </button>
    </div>
  );
}
