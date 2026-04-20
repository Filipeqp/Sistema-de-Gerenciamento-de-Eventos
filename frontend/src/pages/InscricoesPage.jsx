import { useEffect, useState } from 'react';
import { eventoAPI, inscricaoAPI, participanteAPI } from '../api';

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

const empty = { idEvento: '', idParticipante: '', dataInscricao: '' };

export default function InscricoesPage() {
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

  useEffect(() => {
    carregar();
    carregarRefs();
  }, []);

  const carregar = async () => {
    try {
      const res = await inscricaoAPI.listar();
      setLista(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      toast_(err.response?.data?.erro || 'Erro ao carregar inscricoes', 'error');
    } finally {
      setLoading(false);
    }
  };

  const carregarRefs = async () => {
    try {
      const [eventosRes, participantesRes] = await Promise.all([eventoAPI.listar(), participanteAPI.listar()]);
      setEventos(Array.isArray(eventosRes.data) ? eventosRes.data : []);
      setParticipantes(Array.isArray(participantesRes.data) ? participantesRes.data : []);
    } catch {
    }
  };

  const nomeEvento = (id) => eventos.find((evento) => evento.id === id)?.nome || `Evento #${id}`;
  const nomeParticipante = (id) => participantes.find((participante) => participante.id === id)?.nome || `Participante #${id}`;

  const abrir = (inscricao = null) => {
    setEditando(inscricao);
    setForm(
      inscricao
        ? { idEvento: inscricao.idEvento, idParticipante: inscricao.idParticipante, dataInscricao: inscricao.dataInscricao }
        : empty
    );
    setShowModal(true);
  };

  const salvar = async () => {
    if (!form.idEvento || !form.idParticipante || !form.dataInscricao) {
      toast_('Todos os campos sao obrigatorios', 'error');
      return;
    }

    try {
      const payload = {
        ...form,
        idEvento: parseInt(form.idEvento, 10),
        idParticipante: parseInt(form.idParticipante, 10),
      };
      if (editando) {
        await inscricaoAPI.atualizar(editando.id, payload);
        toast_('Inscricao atualizada');
      } else {
        await inscricaoAPI.criar(payload);
        toast_('Inscricao realizada');
      }
      setShowModal(false);
      await carregar();
    } catch (err) {
      toast_(err.response?.data?.erro || 'Erro ao salvar inscricao', 'error');
    }
  };

  const excluir = async (id) => {
    if (!confirm('Cancelar esta inscricao?')) return;

    try {
      await inscricaoAPI.excluir(id);
      toast_('Inscricao cancelada');
      await carregar();
    } catch (err) {
      toast_(err.response?.data?.erro || 'Erro ao cancelar inscricao', 'error');
    }
  };

  const filtrado = lista.filter((inscricao) => filtroEvento === '' || inscricao.idEvento === parseInt(filtroEvento, 10));

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">
          Inscricoes <span>IN</span>
        </h1>
        <button className="btn btn-primary" onClick={() => abrir()}>
          + Nova Inscricao
        </button>
      </div>

      <div className="search-bar">
        <select
          className="form-select"
          style={{ maxWidth: '280px' }}
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
          <div className="empty-icon">IN</div>
          <p className="empty-text">Nenhuma inscricao encontrada</p>
        </div>
      ) : (
        <div className="grid">
          {filtrado.map((inscricao) => (
            <div key={inscricao.id} className="card">
              <div className="card-title">Inscricao #{inscricao.id}</div>
              <div className="card-field">
                <span className="card-label">Evento</span>
                <span className="card-value badge badge-purple">{nomeEvento(inscricao.idEvento)}</span>
              </div>
              <div className="card-field">
                <span className="card-label">Participante</span>
                <span className="card-value badge badge-pink">{nomeParticipante(inscricao.idParticipante)}</span>
              </div>
              <div className="card-field">
                <span className="card-label">Data</span>
                <span className="card-value">{inscricao.dataInscricao}</span>
              </div>
              <div className="card-actions">
                <button className="btn btn-ghost btn-sm" onClick={() => abrir(inscricao)}>
                  Editar
                </button>
                <button className="btn btn-danger btn-sm" onClick={() => excluir(inscricao.id)}>
                  Cancelar
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {showModal && (
        <Modal title={editando ? 'Editar Inscricao' : 'Nova Inscricao'} onClose={() => setShowModal(false)}>
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
          <div className="form-group">
            <label className="form-label">Participante</label>
            <select
              className="form-select"
              value={form.idParticipante}
              onChange={(e) => setForm({ ...form, idParticipante: e.target.value })}
            >
              <option value="">Selecione um participante</option>
              {participantes.map((participante) => (
                <option key={participante.id} value={participante.id}>
                  {participante.nome}
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Data da inscricao (dd/mm/aaaa)</label>
            <input
              className="form-input"
              value={form.dataInscricao}
              onChange={(e) => setForm({ ...form, dataInscricao: e.target.value })}
              placeholder="13/04/2026"
            />
          </div>
          <div className="modal-actions">
            <button className="btn btn-ghost" onClick={() => setShowModal(false)}>
              Cancelar
            </button>
            <button className="btn btn-primary" onClick={salvar}>
              {editando ? 'Salvar' : 'Inscrever'}
            </button>
          </div>
        </Modal>
      )}

      {toast && <Toast msg={toast.msg} type={toast.type} onClose={() => setToast(null)} />}
    </div>
  );
}
