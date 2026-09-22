import { useEffect, useState } from 'react';
import { api } from '../api.js';

export default function HistoryScreen({ onBack }) {
  const [items, setItems] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api
      .history()
      .then(setItems)
      .catch((e) => setError(e.message));
  }, []);

  return (
    <div className="screen">
      <div className="topbar">
        <div className="logo-small">
          <span className="logo-dot" /> История
        </div>
        <div className="actions">
          <button type="button" className="btn-link" onClick={onBack}>
            Назад
          </button>
        </div>
      </div>

      {error && <div className="empty-state">{error}</div>}
      {!error && items === null && <div className="empty-state">Загружаем...</div>}
      {!error && items !== null && items.length === 0 && (
        <div className="empty-state">Анализов пока нет</div>
      )}
      {!error &&
        items !== null &&
        items.map((item) => (
          <div className="history-item" key={item.id}>
            <div className="row-top">
              <span className="score">{item.score}</span>
              <span className="level">{item.level}</span>
            </div>
            <div className="verdict-short">{item.verdict || ''}</div>
          </div>
        ))}
    </div>
  );
}
