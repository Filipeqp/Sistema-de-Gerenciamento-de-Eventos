import { useState, useEffect } from 'react';
import { palestranteAPI, eventoAPI } from '../api';

function Toast({ msg, type, onClose }) {
  useEffect(() => { const t = setTimeout(onClose, 3000); return () => clearTimeout(t); }, []);
  return <div className={`toast toast-${type}`}>{msg}</div>;
}

function Modal({ title, onClose, children }) {
  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal"><h2 className="modal-title">{title}</h2>{children}</div>
    </div>
  );
}

const empty = { nome: '', miniCurriculo: '', especialidades: '', idEvento: '' };

export default function Palestrantes() {
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

  useEffect(() => { carregar(); carregarEventos(); }, []);

  const carregar = async () => {
    try { const res = await palestranteAPI.listar(); setLista(res.data); }
    catch { toast_('Erro ao carregar', 'error'); }
    setLoading(false);
  };

  const carregarEventos = async () => {
    try { const res = await eventoAPI.listar(); setEventos(res.data); }
    catch {}
  };

  const nomeEvento = (id) => eventos.find(e => e.id === id)?.nome || `Evento #${id}`;

  const abrir = (p = null) => {
    setEditando(p);
    setForm(p ? { nome: p.nome, miniCurriculo: p.miniCurriculo, especialidades: p.especialidades, idEvento: p.idEvento } : empty);
    setShowModal(true);
  };

  const salvar = async () => {
    if (!form.nome.trim() || !form.idEvento) { toast_('Nome e Evento são obrigatórios', 'error'); return; }
    try {
      const payload = { ...form, idEvento: parseInt(form.idEvento) };
      if (editando) { await palestranteAPI.atualizar(editando.id, payload); toast_('Palestrante atualizado!'); }
      else { await palestranteAPI.criar(payload); toast_('Palestrante criado!'); }
      setShowModal(false); carregar();
    } catch { toast_('Erro ao salvar', 'error'); }
  };

  const excluir = async (id) => {
    if (!confirm('Excluir este palestrante?')) return;
    try { await palestranteAPI.excluir(id); toast_('Excluído!'); carregar(); }
    catch { toast_('Erro ao excluir', 'error'); }
  };

  const filtrado = lista.filter(p =>
    p.nome.toLowerCase().includes(busca.toLowerCase()) &&
    (filtroEvento === '' || p.idEvento === parseInt(filtroEvento))
  );

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Palestrantes <span>◈</span></h1>
        <button className="btn btn-primary" onClick={() => abrir()}>+ Novo Palestrante</button>
      </div>

      <div className="search-bar">
        <input className="search-input" placeholder="Buscar palestrante..." value={busca} onChange={e => setBusca(e.target.value)} />
        <select className="form-select" style={{width:'200px'}} value={filtroEvento} onChange={e => setFiltroEvento(e.target.value)}>
          <option value="">Todos os eventos</option>
          {eventos.map(ev => <option key={ev.id} value={ev.id}>{ev.nome}</option>)}
        </select>
      </div>

      {loading ? <div className="loading">Carregando...</div> :
        filtrado.length === 0 ? (
          <div className="empty"><div className="empty-icon">🎤</div><p className="empty-text">Nenhum palestrante encontrado</p></div>
        ) : (
          <div className="grid">
            {filtrado.map(p => (
              <div key={p.id} className="card">
                <div className="card-title">{p.nome}</div>
                <div className="card-field"><span className="card-label">Currículo</span><span className="card-value">{p.miniCurriculo}</span></div>
                <div className="card-field">
                  <span className="card-label">Especialidades</span>
                  <span className="card-value">{p.especialidades.split(',').map((s,i) => <span key={i} className="tag">{s.trim()}</span>)}</span>
                </div>
                <div className="card-field"><span className="card-label">Evento</span><span className="card-value badge badge-purple">{nomeEvento(p.idEvento)}</span></div>
                <div className="card-actions">
                  <button className="btn btn-ghost btn-sm" onClick={() => abrir(p)}>✏️ Editar</button>
                  <button className="btn btn-danger btn-sm" onClick={() => excluir(p.id)}>🗑️ Excluir</button>
                </div>
              </div>
            ))}
          </div>
        )
      }

      {showModal && (
        <Modal title={editando ? 'Editar Palestrante' : 'Novo Palestrante'} onClose={() => setShowModal(false)}>
          <div className="form-group">
            <label className="form-label">Nome</label>
            <input className="form-input" value={form.nome} onChange={e => setForm({...form, nome: e.target.value})} />
          </div>
          <div className="form-group">
            <label className="form-label">Mini Currículo</label>
            <input className="form-input" value={form.miniCurriculo} onChange={e => setForm({...form, miniCurriculo: e.target.value})} />
          </div>
          <div className="form-group">
            <label className="form-label">Especialidades (separadas por vírgula)</label>
            <input className="form-input" value={form.especialidades} onChange={e => setForm({...form, especialidades: e.target.value})} />
          </div>
          <div className="form-group">
            <label className="form-label">Evento</label>
            <select className="form-select" value={form.idEvento} onChange={e => setForm({...form, idEvento: e.target.value})}>
              <option value="">Selecione um evento</option>
              {eventos.map(ev => <option key={ev.id} value={ev.id}>{ev.nome}</option>)}
            </select>
          </div>
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
