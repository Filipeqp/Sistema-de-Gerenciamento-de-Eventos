import axios from 'axios';

const host = typeof window !== 'undefined' ? window.location.hostname : '127.0.0.1';
const BASE = `http://${host || '127.0.0.1'}:8080`;

export const eventoAPI = {
  listar: () => axios.get(`${BASE}/eventos`),
  buscar: (id) => axios.get(`${BASE}/eventos/${id}`),
  criar: (data) => axios.post(`${BASE}/eventos`, data),
  atualizar: (id, data) => axios.put(`${BASE}/eventos/${id}`, data),
  excluir: (id) => axios.delete(`${BASE}/eventos/${id}`),
};

export const palestranteAPI = {
  listar: () => axios.get(`${BASE}/palestrantes`),
  buscar: (id) => axios.get(`${BASE}/palestrantes/${id}`),
  listarPorEvento: (idEvento) => axios.get(`${BASE}/palestrantes?idEvento=${idEvento}`),
  criar: (data) => axios.post(`${BASE}/palestrantes`, data),
  atualizar: (id, data) => axios.put(`${BASE}/palestrantes/${id}`, data),
  excluir: (id) => axios.delete(`${BASE}/palestrantes/${id}`),
};

export const participanteAPI = {
  listar: () => axios.get(`${BASE}/participantes`),
  buscar: (id) => axios.get(`${BASE}/participantes/${id}`),
  criar: (data) => axios.post(`${BASE}/participantes`, data),
  atualizar: (id, data) => axios.put(`${BASE}/participantes/${id}`, data),
  excluir: (id) => axios.delete(`${BASE}/participantes/${id}`),
};

export const inscricaoAPI = {
  listar: () => axios.get(`${BASE}/inscricoes`),
  buscar: (id) => axios.get(`${BASE}/inscricoes/${id}`),
  listarPorEvento: (idEvento) => axios.get(`${BASE}/inscricoes?idEvento=${idEvento}`),
  listarPorParticipante: (idPart) => axios.get(`${BASE}/inscricoes?idParticipante=${idPart}`),
  criar: (data) => axios.post(`${BASE}/inscricoes`, data),
  atualizar: (id, data) => axios.put(`${BASE}/inscricoes/${id}`, data),
  excluir: (id) => axios.delete(`${BASE}/inscricoes/${id}`),
};
