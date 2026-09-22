export default function LoadingScreen() {
  return (
    <div className="screen">
      <div className="loading-wrap">
        <div className="loading-ring">
          <div className="loading-ring-inner" />
        </div>
        <p className="dim">Считываем твою ауру...</p>
      </div>
    </div>
  );
}
