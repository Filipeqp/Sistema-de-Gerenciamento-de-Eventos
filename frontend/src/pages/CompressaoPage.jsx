import { useState } from 'react';
import { compressaoAPI } from '../api';

export default function CompressaoPage() {
  const [resultado, setResultado] = useState(null);
  const [erro, setErro] = useState('');
  const [carregando, setCarregando] = useState('');

  async function gerar(tipo) {
    setErro('');
    setCarregando(tipo);

    try {
      const response = tipo === 'huffman'
        ? await compressaoAPI.gerarHuffman()
        : await compressaoAPI.gerarLzw();
      setResultado(response.data);
    } catch (error) {
      setErro(error.response?.data?.erro || 'Nao foi possivel gerar a compressao');
    } finally {
      setCarregando('');
    }
  }

  return (
    <section>
      <div className="page-header">
        <h1 className="page-title">Compressao <span>Fase IV</span></h1>
      </div>

      <div className="compression-actions">
        <button
          className="btn btn-primary"
          onClick={() => gerar('huffman')}
          disabled={Boolean(carregando)}
        >
          {carregando === 'huffman' ? 'Gerando...' : 'Gerar Huffman'}
        </button>
        <button
          className="btn btn-ghost"
          onClick={() => gerar('lzw')}
          disabled={Boolean(carregando)}
        >
          {carregando === 'lzw' ? 'Gerando...' : 'Gerar LZW'}
        </button>
      </div>

      {erro && <div className="toast toast-error">{erro}</div>}

      {resultado && (
        <div className="card compression-result">
          <h2 className="card-title">{resultado.algoritmo.toUpperCase()}</h2>
          <div className="card-field">
            <span className="card-label">Arquivos</span>
            <span className="card-value">{resultado.arquivosCompactados}</span>
          </div>
          <div className="card-field">
            <span className="card-label">Original</span>
            <span className="card-value">{resultado.tamanhoOriginal} bytes</span>
          </div>
          <div className="card-field">
            <span className="card-label">Compactado</span>
            <span className="card-value">{resultado.tamanhoCompactado} bytes</span>
          </div>
          <div className="card-field">
            <span className="card-label">Taxa</span>
            <span className="card-value">{resultado.taxaCompressao}</span>
          </div>
          <div className="card-field">
            <span className="card-label">Arquivo</span>
            <span className="card-value">{resultado.arquivo}</span>
          </div>
        </div>
      )}
    </section>
  );
}
