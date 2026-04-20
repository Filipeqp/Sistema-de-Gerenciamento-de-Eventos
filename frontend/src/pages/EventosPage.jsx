import { useEffect, useState } from 'react';
import { eventoAPI } from '../api';

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

const empty = { nome: '', descricao: '', dataEvento: '', preco: '', tags: '' };

const normalizarEvento = (evento) => ({
  ...evento,
  nome: evento?.nome ?? '',
  descricao: evento?.descricao ?? '',
  dataEvento: evento?.dataEvento ?? '',
  preco: Number(evento?.preco ?? 0),
  tags: evento?.tags ?? '',
});

export default function EventosPage() {
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
      toast_(err.response?.data?.erro || 'Erro ao carregar eventos', 'error');
    } finally {
      setLoading(false);
    }
  };

  const abrir = (evento = null) => {
    setEditando(evento);
    setForm(
      evento
        ? {
            nome: evento.nome,
            descricao: evento.descricao,
            dataEvento: evento.dataEvento,
            preco: String(evento.preco ?? ''),
            tags: evento.tags,
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
        await eventoAPI.atualizar(editando.id, payload);
        toast_('Evento atualizado');
      } else {
        await eventoAPI.criar(payload);
        toast_('Evento criado');
      }

      setForm(empty);
      setShowModal(false);
      await carregar();
    } catch (err) {
      toast_(err.response?.data?.erro || 'Erro ao salvar evento', 'error');
    }
  };

  const excluir = async (id) => {
    if (!confirm('Excluir este evento?')) return;

    try {
      await eventoAPI.excluir(id);
      toast_('Evento excluido');
      await carregar();
    } catch (err) {
      toast_(err.response?.data?.erro || 'Erro ao excluir evento', 'error');
    }
  };

  const filtrado = lista.filter((evento) => evento.nome.toLowerCase().includes(busca.toLowerCase()));

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">
          Eventos <span>EV</span>
        </h1>
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
          <div className="empty-icon">EV</div>
          <p className="empty-text">Nenhum evento encontrado</p>
        </div>
      ) : (
        <div className="grid">
          {filtrado.map((evento) => (
            <div key={evento.id} className="card">
              <div className="card-title">{evento.nome}</div>
              <div className="card-field">
                <span className="card-label">Descricao</span>
                <span className="card-value">{evento.descricao}</span>
              </div>
              <div className="card-field">
                <span className="card-label">Data</span>
                <span className="card-value">{evento.dataEvento}</span>
              </div>
              <div className="card-field">
                <span className="card-label">Preco</span>
                <span className="card-value badge badge-green">R$ {evento.preco.toFixed(2)}</span>
              </div>
              <div className="card-field">
                <span className="card-label">Tags</span>
                <span className="card-value">
                  {(evento.tags || '')
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
                <button className="btn btn-ghost btn-sm" onClick={() => abrir(evento)}>
                  Editar
                </button>
                <button className="btn btn-danger btn-sm" onClick={() => excluir(evento.id)}>
                  Excluir
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {showModal && (
        <Modal title={editando ? 'Editar Evento' : 'Novo Evento'} onClose={() => setShowModal(false)}>
          {['nome', 'descricao', 'dataEvento', 'preco', 'tags'].map((field) => (
            <div key={field} className="form-group">
              <label className="form-label">
                {field === 'dataEvento'
                  ? 'Data (dd/mm/aaaa)'
                  : field === 'preco'
                    ? 'Preco'
                    : field === 'tags'
                      ? 'Tags (separadas por virgula)'
                      : field.charAt(0).toUpperCase() + field.slice(1)}
              </label>
              <input
                className="form-input"
                value={form[field]}
                onChange={(e) => setForm({ ...form, [field]: e.target.value })}
                placeholder={field === 'tags' ? 'tecnologia, java, aeds' : ''}
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
