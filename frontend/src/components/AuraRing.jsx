export default function AuraRing({ score, level }) {
  return (
    <div className="aura-ring-wrap">
      <div className="aura-ring">
        <div className="aura-ring-inner">
          <div className="score-num">{score ?? '--'}</div>
          <div className="score-level">{level || 'LEVEL'}</div>
        </div>
      </div>
    </div>
  );
}
