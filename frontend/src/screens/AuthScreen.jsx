import { useState } from 'react';
import { api } from '../api.js';

export default function AuthScreen({ onAuth }) {
  const [mode, setMode] = useState('login');
  const [login, setLogin] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const isRegister = mode === 'register';

  const toggleMode = () => {
    setMode(isRegister ? 'login' : 'register');
    setError('');
  };

  const submit = async (e) => {
    e.preventDefault();
    setError('');

    if (!login.trim() || !password) {
      setError('Заполни все поля');
      return;
    }
    if (isRegister && !email.trim()) {
      setError('Заполни все поля');
      return;
    }

    setBusy(true);
    try {
      const data = isRegister
        ? await api.register(login.trim(), email.trim(), password)
        : await api.login(login.trim(), password);
      onAuth(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="screen">
      <div className="brand">
        <div className="ring-mini">
          <div className="ring-mini-inner" />
        </div>
        <h1 className="display">Aura</h1>
        <p>Узнай свою ауру по фото и описанию себя</p>
      </div>

      {error && <div className="error-msg">{error}</div>}

      <form onSubmit={submit}>
        {isRegister && (
          <div className="field">
            <label htmlFor="input-email">Email</label>
            <input
              id="input-email"
              type="email"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>
        )}
        <div className="field">
          <label htmlFor="input-login">{isRegister ? 'Username' : 'Username или email'}</label>
          <input
            id="input-login"
            type="text"
            placeholder="username"
            value={login}
            onChange={(e) => setLogin(e.target.value)}
          />
        </div>
        <div className="field">
          <label htmlFor="input-password">Пароль</label>
          <input
            id="input-password"
            type="password"
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>

        <button className="btn-primary" type="submit" disabled={busy}>
          {isRegister ? 'Зарегистрироваться' : 'Войти'}
        </button>
      </form>

      <div className="link-row">
        <button type="button" className="btn-link" onClick={toggleMode}>
          {isRegister ? 'Уже есть аккаунт? Войти' : 'Нет аккаунта? Зарегистрироваться'}
        </button>
      </div>
    </div>
  );
}
