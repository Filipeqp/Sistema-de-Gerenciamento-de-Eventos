import { useState } from 'react';
import { pesquisaAPI } from '../api';

const filtros = {
  algoritmo: 'bm',
  entidade: 'todos',
  padrao: '',
};

export default function PesquisaPage() {
  const [form, setForm] = useState(filtros);
  const [resultado, setResultado] = useState(null);
  const [erro, setErro] = useState('');
  const [carregando, setCarregando] = useState(false);

  async function pesquisar() {
    if (!form.padrao.trim()) {
      setErro('Informe um padrao para pesquisar');
      return;
    }

    setErro('');
    setCarregando(true);
    try {
      const response = await pesquisaAPI.buscar(form);
      setResultado(response.data);
    } catch (error) {
      setErro(error.response?.data?.erro || 'Nao foi possivel executar a pesquisa');
      setResultado(null);
    } finally {
      setCarregando(false);
    }
  }

  return (
    <section>
      <div className="page-header">
        <h1 className="page-title">Casamento de Padroes <span>Fase IV</span></h1>
      </div>

      <div className="card search-panel">
        <div className="search-grid">
          <div className="form-group">
            <label className="form-label">Algoritmo</label>
            <select
              className="form-select"
              value={form.algoritmo}
              onChange={(e) => setForm({ ...form, algoritmo: e.target.value })}
            >
              <option value="bm">Boyer-Moore</option>
              <option value="kmp">KMP</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Entidade</label>
            <select
              className="form-select"
              value={form.entidade}
              onChange={(e) => setForm({ ...form, entidade: e.target.value })}
            >
              <option value="todos">Todas</option>
              <option value="eventos">Eventos</option>
              <option value="palestrantes">Palestrantes</option>
              <option value="participantes">Participantes</option>
              <option value="inscricoes">Inscricoes</option>
            </select>
          </div>
          <div className="form-group search-grid-wide">
            <label className="form-label">Padrao</label>
            <input
              className="form-input"
              placeholder="Ex.: java, backend, 15/06/2026..."
              value={form.padrao}
              onChange={(e) => setForm({ ...form, padrao: e.target.value })}
            />
          </div>
        </div>
        <div className="modal-actions">
          <button className="btn btn-primary" onClick={pesquisar} disabled={carregando}>
            {carregando ? 'Pesquisando...' : 'Executar pesquisa'}
          </button>
        </div>
      </div>

      {erro && <div className="toast toast-error">{erro}</div>}

      {resultado && (
        <div className="search-results">
          <div className="card result-summary">
            <div className="card-title">Resumo da busca</div>
            <div className="card-field">
              <span className="card-label">Algoritmo</span>
              <span className="card-value">{resultado.algoritmo.toUpperCase()}</span>
            </div>
            <div className="card-field">
              <span className="card-label">Entidade</span>
              <span className="card-value">{resultado.entidade}</span>
            </div>
            <div className="card-field">
              <span className="card-label">Ocorrencias</span>
              <span className="card-value">{resultado.quantidade}</span>
            </div>
          </div>

          {resultado.resultados?.length ? (
            <div className="grid">
              {resultado.resultados.map((item) => (
                <div key={`${item.entidade}-${item.id}`} className="card">
                  <div className="card-title">{item.titulo}</div>
                  <div className="card-field">
                    <span className="card-label">Entidade</span>
                    <span className="card-value">{item.entidade}</span>
                  </div>
                  <div className="card-field">
                    <span className="card-label">ID</span>
                    <span className="card-value">{item.id}</span>
                  </div>
                  <div className="card-field">
                    <span className="card-label">Ocorrencias</span>
                    <span className="card-value">{item.totalOcorrencias}</span>
                  </div>
                  <div className="card-field">
                    <span className="card-label">Posicoes</span>
                    <span className="card-value">{item.posicoes.join(', ')}</span>
                  </div>
                  <div className="card-field">
                    <span className="card-label">Trecho</span>
                    <span className="card-value">{item.trecho}</span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="empty">
              <div className="empty-icon">BM</div>
              <p className="empty-text">Nenhuma ocorrencia encontrada para o padrao informado</p>
            </div>
          )}
        </div>
      )}
    </section>
  );
}
