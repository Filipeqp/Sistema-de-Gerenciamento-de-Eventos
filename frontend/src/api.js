import axios from 'axios';

const host = typeof window !== 'undefined' ? window.location.hostname : '127.0.0.1';
const BASE = `http://${host || '127.0.0.1'}:8080`;

export const eventoAPI = {
  listar: (params = {}) => axios.get(`${BASE}/eventos`, { params }),
  buscar: (id) => axios.get(`${BASE}/eventos/${id}`),
  criar: (data) => axios.post(`${BASE}/eventos`, data),
  atualizar: (id, data) => axios.put(`${BASE}/eventos/${id}`, data),
  excluir: (id) => axios.delete(`${BASE}/eventos/${id}`),
};

export const palestranteAPI = {
  listar: (params = {}) => axios.get(`${BASE}/palestrantes`, { params }),
  buscar: (id) => axios.get(`${BASE}/palestrantes/${id}`),
  listarPorEvento: (idEvento, params = {}) => axios.get(`${BASE}/palestrantes`, { params: { ...params, idEvento } }),
  criar: (data) => axios.post(`${BASE}/palestrantes`, data),
  atualizar: (id, data) => axios.put(`${BASE}/palestrantes/${id}`, data),
  excluir: (id) => axios.delete(`${BASE}/palestrantes/${id}`),
};

export const participanteAPI = {
  listar: (params = {}) => axios.get(`${BASE}/participantes`, { params }),
  buscar: (id) => axios.get(`${BASE}/participantes/${id}`),
  criar: (data) => axios.post(`${BASE}/participantes`, data),
  atualizar: (id, data) => axios.put(`${BASE}/participantes/${id}`, data),
  excluir: (id) => axios.delete(`${BASE}/participantes/${id}`),
};

export const inscricaoAPI = {
  listar: (params = {}) => axios.get(`${BASE}/inscricoes`, { params }),
  buscar: (id) => axios.get(`${BASE}/inscricoes/${id}`),
  listarPorEvento: (idEvento, params = {}) => axios.get(`${BASE}/inscricoes`, { params: { ...params, idEvento } }),
  listarPorParticipante: (idParticipante, params = {}) => axios.get(`${BASE}/inscricoes`, { params: { ...params, idParticipante } }),
  criar: (data) => axios.post(`${BASE}/inscricoes`, data),
  atualizar: (id, data) => axios.put(`${BASE}/inscricoes/${id}`, data),
  excluir: (id) => axios.delete(`${BASE}/inscricoes/${id}`),
};

export const compressaoAPI = {
  gerarHuffman: () => axios.post(`${BASE}/compressao/huffman`),
  gerarLzw: () => axios.post(`${BASE}/compressao/lzw`),
  restaurarHuffman: () => axios.post(`${BASE}/compressao/restaurar/huffman`),
  restaurarLzw: () => axios.post(`${BASE}/compressao/restaurar/lzw`),
};

export const pesquisaAPI = {
  buscar: (params = {}) => axios.get(`${BASE}/pesquisa`, { params }),
};

export const authAPI = {
  status: () => axios.get(`${BASE}/auth/status`),
  login: (data) => axios.post(`${BASE}/auth/login`, data),
  primeiroAcesso: (data) => axios.post(`${BASE}/auth/primeiro-acesso`, data),
  registrar: (data) => axios.post(`${BASE}/auth/register`, data),
};
