import { useState, useEffect } from 'react';
import { eventoAPI } from '../api';

function Toast({ msg, type, onClose }) {
  useEffect(() => {
    const t = setTimeout(onClose, 3000);
    return () => clearTimeout(t);
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

const empty = { nome: '', descricao: '', dataEvento: '', preco: '', tags: '' };

const normalizarEvento = (ev) => ({
  ...ev,
  nome: ev?.nome ?? '',
  descricao: ev?.descricao ?? '',
  dataEvento: ev?.dataEvento ?? '',
  preco: Number(ev?.preco ?? 0),
  tags: ev?.tags ?? '',
});

export default function Eventos() {
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
      const res = await eventoAPI.listar();
      setLista(Array.isArray(res.data) ? res.data.map(normalizarEvento) : []);
    } catch (err) {
      const msg = err.response?.data?.erro || 'Erro ao carregar eventos';
      toast_(msg, 'error');
    } finally {
      setLoading(false);
    }
  };

  const abrir = (ev = null) => {
    setEditando(ev);
    setForm(
      ev
        ? {
            nome: ev.nome,
            descricao: ev.descricao,
            dataEvento: ev.dataEvento,
            preco: String(ev.preco ?? ''),
            tags: ev.tags,
          }
        : empty
    );
    setShowModal(true);
  };

  const salvar = async () => {
    if (!form.nome.trim()) {
      toast_('Nome e obrigatorio', 'error');
      return;
    }

    try {
      const payload = {
        nome: form.nome.trim(),
        descricao: form.descricao.trim(),
        dataEvento: form.dataEvento.trim(),
        preco: parseFloat(form.preco) || 0,
        tags: form.tags.trim(),
      };

      if (editando) {
        const res = await eventoAPI.atualizar(editando.id, payload);
        const atualizado = normalizarEvento(res.data);
        setLista((atual) => atual.map((item) => (item.id === editando.id ? atualizado : item)));
        toast_('Evento atualizado!');
      } else {
        const res = await eventoAPI.criar(payload);
        const criado = normalizarEvento(res.data);
        setLista((atual) => [criado, ...atual]);
        toast_('Evento criado!');
      }

      setForm(empty);
      setShowModal(false);
      await carregar();
    } catch (err) {
      const msg = err.response?.data?.erro || err.message || 'Erro ao salvar';
      toast_(msg, 'error');
    }
  };

  const excluir = async (id) => {
    if (!confirm('Excluir este evento?')) return;

    try {
      await eventoAPI.excluir(id);
      toast_('Evento excluido!');
      await carregar();
    } catch (err) {
      const msg = err.response?.data?.erro || 'Erro ao excluir';
      toast_(msg, 'error');
    }
  };

  const filtrado = lista.filter((e) => e.nome.toLowerCase().includes(busca.toLowerCase()));

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Eventos <span>?</span></h1>
        <button className="btn btn-primary" onClick={() => abrir()}>
          + Novo Evento
        </button>
      </div>

      <div className="search-bar">
        <input
          className="search-input"
          placeholder="Buscar evento..."
          value={busca}
          onChange={(e) => setBusca(e.target.value)}
        />
      </div>

      {loading ? (
        <div className="loading">Carregando...</div>
      ) : filtrado.length === 0 ? (
        <div className="empty">
          <div className="empty-icon">??</div>
          <p className="empty-text">Nenhum evento encontrado</p>
        </div>
      ) : (
        <div className="grid">
          {filtrado.map((ev) => (
            <div key={ev.id} className="card">
              <div className="card-title">{ev.nome}</div>
              <div className="card-field">
                <span className="card-label">Descricao</span>
                <span className="card-value">{ev.descricao}</span>
              </div>
              <div className="card-field">
                <span className="card-label">Data</span>
                <span className="card-value">{ev.dataEvento}</span>
              </div>
              <div className="card-field">
                <span className="card-label">Preco</span>
                <span className="card-value badge badge-green">R$ {Number(ev.preco).toFixed(2)}</span>
              </div>
              <div className="card-field">
                <span className="card-label">Tags</span>
                <span className="card-value">
                  {ev.tags
                    .split(',')
                    .map((t) => t.trim())
                    .filter(Boolean)
                    .map((t, i) => (
                      <span key={i} className="tag">
                        {t}
                      </span>
                    ))}
                </span>
              </div>
              <div className="card-actions">
                <button className="btn btn-ghost btn-sm" onClick={() => abrir(ev)}>
                  Editar
                </button>
                <button className="btn btn-danger btn-sm" onClick={() => excluir(ev.id)}>
                  Excluir
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {showModal && (
        <Modal title={editando ? 'Editar Evento' : 'Novo Evento'} onClose={() => setShowModal(false)}>
          {['nome', 'descricao', 'dataEvento', 'preco', 'tags'].map((f) => (
            <div key={f} className="form-group">
              <label className="form-label">
                {f === 'dataEvento'
                  ? 'Data (dd/mm/aaaa)'
                  : f === 'preco'
                    ? 'Preco'
                    : f === 'tags'
                      ? 'Tags (separadas por virgula)'
                      : f.charAt(0).toUpperCase() + f.slice(1)}
              </label>
              <input
                className="form-input"
                value={form[f]}
                onChange={(e) => setForm({ ...form, [f]: e.target.value })}
                placeholder={f === 'tags' ? 'tecnologia, java, aeds' : ''}
              />
            </div>
          ))}
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
