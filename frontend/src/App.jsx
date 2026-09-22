import { useState } from 'react';
import { api, getToken, setToken, clearToken } from './api.js';
import AuthScreen from './screens/AuthScreen.jsx';
import HomeScreen from './screens/HomeScreen.jsx';
import LoadingScreen from './screens/LoadingScreen.jsx';
import ResultScreen from './screens/ResultScreen.jsx';
import HistoryScreen from './screens/HistoryScreen.jsx';

export default function App() {
  const [view, setView] = useState(() => (getToken() ? 'home' : 'auth'));
  const [result, setResult] = useState(null);
  const [analyzeError, setAnalyzeError] = useState('');

  const handleAuth = (data) => {
    setToken(data.token);
    setAnalyzeError('');
    setView('home');
  };

  const handleLogout = () => {
    clearToken();
    setResult(null);
    setView('auth');
  };

  const handleAnalyze = async (file, aboutMe) => {
    setView('loading');
    try {
      setResult(await api.analyze(file, aboutMe));
      setView('result');
    } catch (e) {
      setAnalyzeError(e.message);
      setView(getToken() ? 'home' : 'auth');
    }
  };

  return (
    <>
      {view === 'auth' && <AuthScreen onAuth={handleAuth} />}
      {view === 'home' && (
        <HomeScreen
          error={analyzeError}
          onAnalyze={handleAnalyze}
          onHistory={() => setView('history')}
          onLogout={handleLogout}
        />
      )}
      {view === 'loading' && <LoadingScreen />}
      {view === 'result' && result && (
        <ResultScreen
          result={result}
          onNew={() => {
            setAnalyzeError('');
            setView('home');
          }}
          onHistory={() => setView('history')}
        />
      )}
      {view === 'history' && (
        <HistoryScreen onBack={() => setView(getToken() ? 'home' : 'auth')} />
      )}
    </>
  );
}
