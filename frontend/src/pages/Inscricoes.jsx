import { useState, useEffect } from 'react';
import { inscricaoAPI, eventoAPI, participanteAPI } from '../api';

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

const empty = { idEvento: '', idParticipante: '', dataInscricao: '' };

export default function Inscricoes() {
  const [lista, setLista] = useState([]);
  const [eventos, setEventos] = useState([]);
  const [participantes, setParticipantes] = useState([]);
  const [form, setForm] = useState(empty);
  const [editando, setEditando] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState(null);
  const [filtroEvento, setFiltroEvento] = useState('');

  const toast_ = (msg, type = 'success') => setToast({ msg, type });

  useEffect(() => { carregar(); carregarRefs(); }, []);

  const carregar = async () => {
    try { const res = await inscricaoAPI.listar(); setLista(res.data); }
    catch { toast_('Erro ao carregar', 'error'); }
    setLoading(false);
  };

  const carregarRefs = async () => {
    try {
      const [ev, part] = await Promise.all([eventoAPI.listar(), participanteAPI.listar()]);
      setEventos(ev.data); setParticipantes(part.data);
    } catch {}
  };

  const nomeEvento = (id) => eventos.find(e => e.id === id)?.nome || `Evento #${id}`;
  const nomeParticipante = (id) => participantes.find(p => p.id === id)?.nome || `Participante #${id}`;

  const abrir = (i = null) => {
    setEditando(i);
    setForm(i ? { idEvento: i.idEvento, idParticipante: i.idParticipante, dataInscricao: i.dataInscricao } : empty);
    setShowModal(true);
  };

  const salvar = async () => {
    if (!form.idEvento || !form.idParticipante || !form.dataInscricao) { toast_('Todos os campos são obrigatórios', 'error'); return; }
    try {
      const payload = { ...form, idEvento: parseInt(form.idEvento), idParticipante: parseInt(form.idParticipante) };
      if (editando) { await inscricaoAPI.atualizar(editando.id, payload); toast_('Inscrição atualizada!'); }
      else { await inscricaoAPI.criar(payload); toast_('Inscrição realizada!'); }
      setShowModal(false); carregar();
    } catch (err) {
      const msg = err.response?.data?.erro || 'Erro ao salvar';
      toast_(msg, 'error');
    }
  };

  const excluir = async (id) => {
    if (!confirm('Cancelar esta inscrição?')) return;
    try { await inscricaoAPI.excluir(id); toast_('Inscrição cancelada!'); carregar(); }
    catch { toast_('Erro ao cancelar', 'error'); }
  };

  const filtrado = lista.filter(i => filtroEvento === '' || i.idEvento === parseInt(filtroEvento));

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Inscrições <span>◈</span></h1>
        <button className="btn btn-primary" onClick={() => abrir()}>+ Nova Inscrição</button>
      </div>

      <div className="search-bar">
        <select className="form-select" style={{maxWidth:'280px'}} value={filtroEvento} onChange={e => setFiltroEvento(e.target.value)}>
          <option value="">Todos os eventos</option>
          {eventos.map(ev => <option key={ev.id} value={ev.id}>{ev.nome}</option>)}
        </select>
      </div>

      {loading ? <div className="loading">Carregando...</div> :
        filtrado.length === 0 ? (
          <div className="empty"><div className="empty-icon">📋</div><p className="empty-text">Nenhuma inscrição encontrada</p></div>
        ) : (
          <div className="grid">
            {filtrado.map(i => (
              <div key={i.id} className="card">
                <div className="card-title">Inscrição #{i.id}</div>
                <div className="card-field"><span className="card-label">Evento</span><span className="card-value badge badge-purple">{nomeEvento(i.idEvento)}</span></div>
                <div className="card-field"><span className="card-label">Participante</span><span className="card-value badge badge-pink">{nomeParticipante(i.idParticipante)}</span></div>
                <div className="card-field"><span className="card-label">Data</span><span className="card-value">{i.dataInscricao}</span></div>
                <div className="card-actions">
                  <button className="btn btn-ghost btn-sm" onClick={() => abrir(i)}>✏️ Editar</button>
                  <button className="btn btn-danger btn-sm" onClick={() => excluir(i.id)}>🗑️ Cancelar</button>
                </div>
              </div>
            ))}
          </div>
        )
      }

      {showModal && (
        <Modal title={editando ? 'Editar Inscrição' : 'Nova Inscrição'} onClose={() => setShowModal(false)}>
          <div className="form-group">
            <label className="form-label">Evento</label>
            <select className="form-select" value={form.idEvento} onChange={e => setForm({...form, idEvento: e.target.value})}>
              <option value="">Selecione um evento</option>
              {eventos.map(ev => <option key={ev.id} value={ev.id}>{ev.nome}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Participante</label>
            <select className="form-select" value={form.idParticipante} onChange={e => setForm({...form, idParticipante: e.target.value})}>
              <option value="">Selecione um participante</option>
              {participantes.map(p => <option key={p.id} value={p.id}>{p.nome}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Data da Inscrição (dd/mm/aaaa)</label>
            <input className="form-input" value={form.dataInscricao} onChange={e => setForm({...form, dataInscricao: e.target.value})} placeholder="13/04/2026" />
          </div>
          <div className="modal-actions">
            <button className="btn btn-ghost" onClick={() => setShowModal(false)}>Cancelar</button>
            <button className="btn btn-primary" onClick={salvar}>{editando ? 'Salvar' : 'Inscrever'}</button>
          </div>
        </Modal>
      )}
      {toast && <Toast msg={toast.msg} type={toast.type} onClose={() => setToast(null)} />}
    </div>
  );
}
