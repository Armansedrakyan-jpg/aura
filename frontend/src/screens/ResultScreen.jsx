import AuraRing from '../components/AuraRing.jsx';

export default function ResultScreen({ result, onNew, onHistory }) {
  return (
    <div className="screen">
      <div className="aura-stage">
        <AuraRing score={result.score} level={result.level} />

        <div className="traits">
          <div className="trait-row">
            <span className="k">Вайб</span>
            <span>{result.vibe || '—'}</span>
          </div>
          <div className="trait-row">
            <span className="k">Энергия</span>
            <span>{result.energy || '—'}</span>
          </div>
          <div className="trait-row">
            <span className="k">Уверенность</span>
            <span>{result.confidence || '—'}</span>
          </div>
          <div className="trait-row">
            <span className="k">Личность</span>
            <span>{result.personality || '—'}</span>
          </div>
        </div>

        <p className="verdict">{result.verdict || '—'}</p>

        <button className="btn-primary" type="button" onClick={onNew}>
          Новый анализ
        </button>
        <button className="btn-ghost" type="button" onClick={onHistory}>
          Смотреть историю
        </button>
      </div>
    </div>
  );
}
