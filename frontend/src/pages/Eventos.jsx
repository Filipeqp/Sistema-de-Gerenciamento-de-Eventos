import { useState, useEffect } from 'react';
import { eventoAPI } from '../api';

function Toast({ msg, type, onClose }) {
  useEffect(() => { const t = setTimeout(onClose, 3000); return () => clearTimeout(t); }, []);
  return <div className={`toast toast-${type}`}>{msg}</div>;
}

function Modal({ title, onClose, children }) {
  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <h2 className="modal-title">{title}</h2>
        {children}
      </div>
    </div>
  );
}

const empty = { nome: '', descricao: '', dataEvento: '', preco: '', tags: '' };

export default function Eventos() {
  const [lista, setLista] = useState([]);
  const [form, setForm] = useState(empty);
  const [editando, setEditando] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState(null);
  const [busca, setBusca] = useState('');

  const toast_ = (msg, type = 'success') => setToast({ msg, type });

  useEffect(() => { carregar(); }, []);

  const carregar = async () => {
    try {
      const res = await eventoAPI.listar();
      setLista(res.data);
    } catch { toast_('Erro ao carregar eventos', 'error'); }
    setLoading(false);
  };

  const abrir = (ev = null) => {
    setEditando(ev);
    setForm(ev ? { nome: ev.nome, descricao: ev.descricao, dataEvento: ev.dataEvento, preco: ev.preco, tags: ev.tags } : empty);
    setShowModal(true);
  };

  const salvar = async () => {
    if (!form.nome.trim()) { toast_('Nome é obrigatório', 'error'); return; }
    try {
      const payload = { ...form, preco: parseFloat(form.preco) || 0 };
      if (editando) { await eventoAPI.atualizar(editando.id, payload); toast_('Evento atualizado!'); }
      else { await eventoAPI.criar(payload); toast_('Evento criado!'); }
      setShowModal(false);
      carregar();
    } catch { toast_('Erro ao salvar', 'error'); }
  };

  const excluir = async (id) => {
    if (!confirm('Excluir este evento?')) return;
    try { await eventoAPI.excluir(id); toast_('Evento excluído!'); carregar(); }
    catch { toast_('Erro ao excluir', 'error'); }
  };

  const filtrado = lista.filter(e => e.nome.toLowerCase().includes(busca.toLowerCase()));

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Eventos <span>◈</span></h1>
        <button className="btn btn-primary" onClick={() => abrir()}>+ Novo Evento</button>
      </div>

      <div className="search-bar">
        <input className="search-input" placeholder="Buscar evento..." value={busca} onChange={e => setBusca(e.target.value)} />
      </div>

      {loading ? <div className="loading">Carregando...</div> :
        filtrado.length === 0 ? (
          <div className="empty"><div className="empty-icon">📅</div><p className="empty-text">Nenhum evento encontrado</p></div>
        ) : (
          <div className="grid">
            {filtrado.map(ev => (
              <div key={ev.id} className="card">
                <div className="card-title">{ev.nome}</div>
                <div className="card-field"><span className="card-label">Descrição</span><span className="card-value">{ev.descricao}</span></div>
                <div className="card-field"><span className="card-label">Data</span><span className="card-value">{ev.dataEvento}</span></div>
                <div className="card-field"><span className="card-label">Preço</span><span className="card-value badge badge-green">R$ {Number(ev.preco).toFixed(2)}</span></div>
                <div className="card-field">
                  <span className="card-label">Tags</span>
                  <span className="card-value">{ev.tags.split(',').map((t,i) => <span key={i} className="tag">{t.trim()}</span>)}</span>
                </div>
                <div className="card-actions">
                  <button className="btn btn-ghost btn-sm" onClick={() => abrir(ev)}>✏️ Editar</button>
                  <button className="btn btn-danger btn-sm" onClick={() => excluir(ev.id)}>🗑️ Excluir</button>
                </div>
              </div>
            ))}
          </div>
        )
      }

      {showModal && (
        <Modal title={editando ? 'Editar Evento' : 'Novo Evento'} onClose={() => setShowModal(false)}>
          {['nome','descricao','dataEvento','preco','tags'].map(f => (
            <div key={f} className="form-group">
              <label className="form-label">{f === 'dataEvento' ? 'Data (dd/mm/aaaa)' : f === 'preco' ? 'Preço' : f === 'tags' ? 'Tags (separadas por vírgula)' : f.charAt(0).toUpperCase() + f.slice(1)}</label>
              <input className="form-input" value={form[f]} onChange={e => setForm({...form, [f]: e.target.value})} placeholder={f === 'tags' ? 'tecnologia, java, aeds' : ''} />
            </div>
          ))}
          <div className="modal-actions">
            <button className="btn btn-ghost" onClick={() => setShowModal(false)}>Cancelar</button>
            <button className="btn btn-primary" onClick={salvar}>{editando ? 'Salvar' : 'Criar'}</button>
          </div>
        </Modal>
      )}

      {toast && <Toast msg={toast.msg} type={toast.type} onClose={() => setToast(null)} />}
    </div>
  );
}
