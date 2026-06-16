import { useEffect, useState } from 'react';
import { authAPI } from '../api';

const initialLoginForm = { email: '', senha: '' };
const initialRegisterForm = { nome: '', email: '', interesses: '', senha: '' };

export default function LoginPage({ onLogin }) {
  const [modo, setModo] = useState('loading');
  const [possuiUsuarios, setPossuiUsuarios] = useState(false);
  const [form, setForm] = useState(initialLoginForm);
  const [erro, setErro] = useState('');
  const [carregando, setCarregando] = useState(false);

  useEffect(() => {
    async function carregarStatus() {
      try {
        const response = await authAPI.status();
        const proximoStatus = Boolean(response.data?.possuiUsuarios);
        // Decide se a tela abre em login ou em cadastro conforme o estado atual da base.
        setPossuiUsuarios(proximoStatus);
        setModo(proximoStatus ? 'login' : 'registro');
        setForm(proximoStatus ? initialLoginForm : initialRegisterForm);
      } catch {
        setErro('Nao foi possivel verificar o status do acesso');
        setPossuiUsuarios(true);
        setModo('login');
        setForm(initialLoginForm);
      }
    }

    carregarStatus();
  }, []);

  async function entrar(e) {
    e.preventDefault();
    setErro('');
    setCarregando(true);

    try {
      // Reaproveita a mesma tela para login, primeiro acesso e criacao de novas contas.
      const response = modo === 'registro'
        ? await (possuiUsuarios ? authAPI.registrar(form) : authAPI.primeiroAcesso(form))
        : await authAPI.login(form);
      onLogin(response.data);
    } catch (error) {
      setErro(error.response?.data?.erro || (modo === 'registro' ? 'Nao foi possivel criar a conta' : 'Nao foi possivel autenticar'));
    } finally {
      setCarregando(false);
    }
  }

  function alternarModo(proximoModo) {
    setErro('');
    setModo(proximoModo);
    setForm(proximoModo === 'registro' ? initialRegisterForm : initialLoginForm);
  }

  return (
    <div className="auth-shell">
      <div className="auth-card">
        <p className="auth-kicker">Fase V</p>
        <h1 className="auth-title">
          {modo === 'registro' ? 'Criar conta com criptografia XOR' : 'Login com criptografia XOR'}
        </h1>
        <p className="auth-copy">
          {modo === 'registro'
            ? (possuiUsuarios
              ? 'Crie um novo participante com senha para ter outro acesso ao sistema.'
              : 'Como ainda nao existe um usuario com senha, cadastre o primeiro participante para liberar o sistema.')
            : 'Entre com o email e a senha cadastrados para um participante. A senha e armazenada no backend usando XOR.'}
        </p>

        {modo === 'loading' ? (
          <div className="loading">Carregando acesso...</div>
        ) : (
        <>
        <div className="auth-switch">
          <button
            type="button"
            className={`auth-switch-btn ${modo === 'login' ? 'active' : ''}`}
            onClick={() => alternarModo('login')}
          >
            Entrar
          </button>
          <button
            type="button"
            className={`auth-switch-btn ${modo === 'registro' ? 'active' : ''}`}
            onClick={() => alternarModo('registro')}
          >
            Criar conta
          </button>
        </div>
        <form onSubmit={entrar}>
          {modo === 'registro' && (
            <>
              <div className="form-group">
                <label className="form-label">Nome</label>
                <input
                  className="form-input"
                  value={form.nome}
                  onChange={(e) => setForm({ ...form, nome: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label className="form-label">Interesses</label>
                <input
                  className="form-input"
                  value={form.interesses}
                  onChange={(e) => setForm({ ...form, interesses: e.target.value })}
                />
              </div>
            </>
          )}
          <div className="form-group">
            <label className="form-label">Email</label>
            <input
              className="form-input"
              type="email"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
            />
          </div>
          <div className="form-group">
            <label className="form-label">Senha</label>
            <input
              className="form-input"
              type="password"
              value={form.senha}
              onChange={(e) => setForm({ ...form, senha: e.target.value })}
            />
          </div>
          {erro && <div className="auth-error">{erro}</div>}
          <button className="btn btn-primary auth-submit" type="submit" disabled={carregando}>
            {carregando
              ? (modo === 'registro' ? 'Criando conta...' : 'Entrando...')
              : (modo === 'registro' ? 'Criar e entrar' : 'Entrar')}
          </button>
        </form>
        </>
        )}
      </div>
    </div>
  );
}
