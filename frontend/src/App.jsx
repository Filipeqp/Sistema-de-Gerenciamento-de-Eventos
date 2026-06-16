import { useState } from 'react';
import { BrowserRouter, Routes, Route, NavLink } from 'react-router-dom';
import Eventos from './pages/EventosPage';
import Palestrantes from './pages/PalestrantesPage';
import Participantes from './pages/ParticipantesPage';
import Inscricoes from './pages/InscricoesPage';
import Compressao from './pages/CompressaoPage';
import Pesquisa from './pages/PesquisaPage';
import LoginPage from './pages/LoginPage';
import './App.css';

export default function App() {
  const [sessao, setSessao] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('gestevent.session') || 'null');
    } catch {
      return null;
    }
  });

  function handleLogin(usuario) {
    localStorage.setItem('gestevent.session', JSON.stringify(usuario));
    setSessao(usuario);
  }

  function handleLogout() {
    localStorage.removeItem('gestevent.session');
    setSessao(null);
  }

  if (!sessao?.autenticado) {
    return <LoginPage onLogin={handleLogin} />;
  }

  return (
    <BrowserRouter>
      <div className="app">
        <header className="header">
          <div className="header-inner">
            <div className="logo">
              <span className="logo-icon">GE</span>
              <span className="logo-text">GestEvent</span>
            </div>
            <nav className="nav">
              <NavLink to="/" end className={({isActive}) => isActive ? 'nav-link active' : 'nav-link'}>Eventos</NavLink>
              <NavLink to="/palestrantes" className={({isActive}) => isActive ? 'nav-link active' : 'nav-link'}>Palestrantes</NavLink>
              <NavLink to="/participantes" className={({isActive}) => isActive ? 'nav-link active' : 'nav-link'}>Participantes</NavLink>
              <NavLink to="/inscricoes" className={({isActive}) => isActive ? 'nav-link active' : 'nav-link'}>Inscricoes</NavLink>
              <NavLink to="/pesquisa" className={({isActive}) => isActive ? 'nav-link active' : 'nav-link'}>Pesquisa</NavLink>
              <NavLink to="/compressao" className={({isActive}) => isActive ? 'nav-link active' : 'nav-link'}>Compressao</NavLink>
            </nav>
            <div className="session-box">
              <span className="session-user">{sessao.nome}</span>
              <button className="btn btn-ghost btn-sm" onClick={handleLogout}>Sair</button>
            </div>
          </div>
        </header>
        <main className="main">
          <Routes>
            <Route path="/" element={<Eventos />} />
            <Route path="/palestrantes" element={<Palestrantes />} />
            <Route path="/participantes" element={<Participantes />} />
            <Route path="/inscricoes" element={<Inscricoes />} />
            <Route path="/pesquisa" element={<Pesquisa />} />
            <Route path="/compressao" element={<Compressao />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}
