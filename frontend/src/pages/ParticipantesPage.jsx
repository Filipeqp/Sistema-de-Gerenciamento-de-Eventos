import { useEffect, useState } from 'react';
import { participanteAPI } from '../api';

function Toast({ msg, type, onClose }) {
  useEffect(() => {
    const timer = setTimeout(onClose, 3000);
    return () => clearTimeout(timer);
  }, [onClose]);

  return <div className={`toast toast-${type}`}>{msg}</div>;
}

function Modal({ title, onClose, children }) {
  return (
    <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <h2 className="modal-title">{title}</h2>
        {children}
      </div>
    </div>
  );
}

const empty = { nome: '', email: '', interesses: '' };

export default function ParticipantesPage() {
  const [lista, setLista] = useState([]);
  const [form, setForm] = useState(empty);
  const [editando, setEditando] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState(null);
  const [busca, setBusca] = useState('');

  const toast_ = (msg, type = 'success') => setToast({ msg, type });

  useEffect(() => {
    carregar();
  }, []);

  const carregar = async () => {
    try {
      const res = await participanteAPI.listar();
      setLista(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      toast_(err.response?.data?.erro || 'Erro ao carregar participantes', 'error');
    } finally {
      setLoading(false);
    }
  };

  const abrir = (participante = null) => {
    setEditando(participante);
    setForm(participante ? { nome: participante.nome, email: participante.email, interesses: participante.interesses } : empty);
    setShowModal(true);
  };

  const salvar = async () => {
    if (!form.nome.trim() || !form.email.trim()) {
      toast_('Nome e email sao obrigatorios', 'error');
      return;
    }

    try {
      if (editando) {
        await participanteAPI.atualizar(editando.id, form);
        toast_('Participante atualizado');
      } else {
        await participanteAPI.criar(form);
        toast_('Participante criado');
      }
      setShowModal(false);
      await carregar();
    } catch (err) {
      toast_(err.response?.data?.erro || 'Erro ao salvar participante', 'error');
    }
  };

  const excluir = async (id) => {
    if (!confirm('Excluir este participante?')) return;

    try {
      await participanteAPI.excluir(id);
      toast_('Participante excluido');
      await carregar();
    } catch (err) {
      toast_(err.response?.data?.erro || 'Erro ao excluir participante', 'error');
    }
  };

  const filtrado = lista.filter((participante) => {
    const termo = busca.toLowerCase();
    return participante.nome.toLowerCase().includes(termo) || participante.email.toLowerCase().includes(termo);
  });

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">
          Participantes <span>PT</span>
        </h1>
        <button className="btn btn-primary" onClick={() => abrir()}>
          + Novo Participante
        </button>
      </div>

      <div className="search-bar">
        <input
          className="search-input"
          placeholder="Buscar por nome ou email..."
          value={busca}
          onChange={(e) => setBusca(e.target.value)}
        />
      </div>

      {loading ? (
        <div className="loading">Carregando...</div>
      ) : filtrado.length === 0 ? (
        <div className="empty">
          <div className="empty-icon">PT</div>
          <p className="empty-text">Nenhum participante encontrado</p>
        </div>
      ) : (
        <div className="grid">
          {filtrado.map((participante) => (
            <div key={participante.id} className="card">
              <div className="card-title">{participante.nome}</div>
              <div className="card-field">
                <span className="card-label">Email</span>
                <span className="card-value">{participante.email}</span>
              </div>
              <div className="card-field">
                <span className="card-label">Interesses</span>
                <span className="card-value">
                  {(participante.interesses || '')
                    .split(',')
                    .map((item) => item.trim())
                    .filter(Boolean)
                    .map((item, index) => (
                      <span key={index} className="tag">
                        {item}
                      </span>
                    ))}
                </span>
              </div>
              <div className="card-actions">
                <button className="btn btn-ghost btn-sm" onClick={() => abrir(participante)}>
                  Editar
                </button>
                <button className="btn btn-danger btn-sm" onClick={() => excluir(participante.id)}>
                  Excluir
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {showModal && (
        <Modal title={editando ? 'Editar Participante' : 'Novo Participante'} onClose={() => setShowModal(false)}>
          <div className="form-group">
            <label className="form-label">Nome</label>
            <input className="form-input" value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} />
          </div>
          <div className="form-group">
            <label className="form-label">Email</label>
            <input className="form-input" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          </div>
          <div className="form-group">
            <label className="form-label">Interesses (separados por virgula)</label>
            <input className="form-input" value={form.interesses} onChange={(e) => setForm({ ...form, interesses: e.target.value })} />
          </div>
          <div className="modal-actions">
            <button className="btn btn-ghost" onClick={() => setShowModal(false)}>
              Cancelar
            </button>
            <button className="btn btn-primary" onClick={salvar}>
              {editando ? 'Salvar' : 'Criar'}
            </button>
          </div>
        </Modal>
      )}

      {toast && <Toast msg={toast.msg} type={toast.type} onClose={() => setToast(null)} />}
    </div>
  );
}
