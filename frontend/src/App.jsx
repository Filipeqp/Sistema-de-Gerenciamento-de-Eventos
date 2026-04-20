import { BrowserRouter, Routes, Route, NavLink } from 'react-router-dom';
import Eventos from './pages/EventosPage';
import Palestrantes from './pages/PalestrantesPage';
import Participantes from './pages/ParticipantesPage';
import Inscricoes from './pages/InscricoesPage';
import './App.css';

export default function App() {
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
            </nav>
          </div>
        </header>
        <main className="main">
          <Routes>
            <Route path="/" element={<Eventos />} />
            <Route path="/palestrantes" element={<Palestrantes />} />
            <Route path="/participantes" element={<Participantes />} />
            <Route path="/inscricoes" element={<Inscricoes />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}
