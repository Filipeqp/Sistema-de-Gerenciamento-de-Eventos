import { useEffect, useState } from 'react';
import { eventoAPI, palestranteAPI } from '../api';

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

const empty = { nome: '', miniCurriculo: '', especialidades: '', idEvento: '' };

export default function PalestrantesPage() {
  const [lista, setLista] = useState([]);
  const [eventos, setEventos] = useState([]);
  const [form, setForm] = useState(empty);
  const [editando, setEditando] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState(null);
  const [busca, setBusca] = useState('');
  const [filtroEvento, setFiltroEvento] = useState('');

  const toast_ = (msg, type = 'success') => setToast({ msg, type });

  useEffect(() => {
    carregar();
    carregarEventos();
  }, []);

  const carregar = async () => {
    try {
      const res = await palestranteAPI.listar();
      setLista(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      toast_(err.response?.data?.erro || 'Erro ao carregar palestrantes', 'error');
    } finally {
      setLoading(false);
    }
  };

  const carregarEventos = async () => {
    try {
      const res = await eventoAPI.listar();
      setEventos(Array.isArray(res.data) ? res.data : []);
    } catch {
    }
  };

  const nomeEvento = (id) => eventos.find((evento) => evento.id === id)?.nome || `Evento #${id}`;

  const abrir = (palestrante = null) => {
    setEditando(palestrante);
    setForm(
      palestrante
        ? {
            nome: palestrante.nome,
            miniCurriculo: palestrante.miniCurriculo,
            especialidades: palestrante.especialidades,
            idEvento: palestrante.idEvento,
          }
        : empty
    );
    setShowModal(true);
  };

  const salvar = async () => {
    if (!form.nome.trim() || !form.idEvento) {
      toast_('Nome e evento sao obrigatorios', 'error');
      return;
    }

    try {
      const payload = { ...form, idEvento: parseInt(form.idEvento, 10) };
      if (editando) {
        await palestranteAPI.atualizar(editando.id, payload);
        toast_('Palestrante atualizado');
      } else {
        await palestranteAPI.criar(payload);
        toast_('Palestrante criado');
      }
      setShowModal(false);
      await carregar();
    } catch (err) {
      toast_(err.response?.data?.erro || 'Erro ao salvar palestrante', 'error');
    }
  };

  const excluir = async (id) => {
    if (!confirm('Excluir este palestrante?')) return;

    try {
      await palestranteAPI.excluir(id);
      toast_('Palestrante excluido');
      await carregar();
    } catch (err) {
      toast_(err.response?.data?.erro || 'Erro ao excluir palestrante', 'error');
    }
  };

  const filtrado = lista.filter((palestrante) => {
    const bateBusca = palestrante.nome.toLowerCase().includes(busca.toLowerCase());
    const bateEvento = filtroEvento === '' || palestrante.idEvento === parseInt(filtroEvento, 10);
    return bateBusca && bateEvento;
  });

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">
          Palestrantes <span>PA</span>
        </h1>
        <button className="btn btn-primary" onClick={() => abrir()}>
          + Novo Palestrante
        </button>
      </div>

      <div className="search-bar">
        <input
          className="search-input"
          placeholder="Buscar palestrante..."
          value={busca}
          onChange={(e) => setBusca(e.target.value)}
        />
        <select
          className="form-select"
          style={{ width: '220px' }}
          value={filtroEvento}
          onChange={(e) => setFiltroEvento(e.target.value)}
        >
          <option value="">Todos os eventos</option>
          {eventos.map((evento) => (
            <option key={evento.id} value={evento.id}>
              {evento.nome}
            </option>
          ))}
        </select>
      </div>

      {loading ? (
        <div className="loading">Carregando...</div>
      ) : filtrado.length === 0 ? (
        <div className="empty">
          <div className="empty-icon">PA</div>
          <p className="empty-text">Nenhum palestrante encontrado</p>
        </div>
      ) : (
        <div className="grid">
          {filtrado.map((palestrante) => (
            <div key={palestrante.id} className="card">
              <div className="card-title">{palestrante.nome}</div>
              <div className="card-field">
                <span className="card-label">Curriculo</span>
                <span className="card-value">{palestrante.miniCurriculo}</span>
              </div>
              <div className="card-field">
                <span className="card-label">Especialidades</span>
                <span className="card-value">
                  {(palestrante.especialidades || '')
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
              <div className="card-field">
                <span className="card-label">Evento</span>
                <span className="card-value badge badge-purple">{nomeEvento(palestrante.idEvento)}</span>
              </div>
              <div className="card-actions">
                <button className="btn btn-ghost btn-sm" onClick={() => abrir(palestrante)}>
                  Editar
                </button>
                <button className="btn btn-danger btn-sm" onClick={() => excluir(palestrante.id)}>
                  Excluir
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {showModal && (
        <Modal title={editando ? 'Editar Palestrante' : 'Novo Palestrante'} onClose={() => setShowModal(false)}>
          <div className="form-group">
            <label className="form-label">Nome</label>
            <input className="form-input" value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} />
          </div>
          <div className="form-group">
            <label className="form-label">Mini curriculo</label>
            <input
              className="form-input"
              value={form.miniCurriculo}
              onChange={(e) => setForm({ ...form, miniCurriculo: e.target.value })}
            />
          </div>
          <div className="form-group">
            <label className="form-label">Especialidades (separadas por virgula)</label>
            <input
              className="form-input"
              value={form.especialidades}
              onChange={(e) => setForm({ ...form, especialidades: e.target.value })}
            />
          </div>
          <div className="form-group">
            <label className="form-label">Evento</label>
            <select className="form-select" value={form.idEvento} onChange={(e) => setForm({ ...form, idEvento: e.target.value })}>
              <option value="">Selecione um evento</option>
              {eventos.map((evento) => (
                <option key={evento.id} value={evento.id}>
                  {evento.nome}
                </option>
              ))}
            </select>
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
