import { useState } from 'react'

function App() {
  // message: バックエンド API からのレスポンステキストを保持する
  const [message, setMessage] = useState<string>('')

  // loading: リクエスト中かどうかを追跡する
  const [loading, setLoading] = useState<boolean>(false)

  // error: リクエスト失敗時のエラーメッセージを保持する
  const [error, setError] = useState<string>('')

  // ボタンがクリックされたときに呼ばれる
  const handleFetch = async () => {
    // 前回の結果をリセット
    setMessage('')
    setError('')
    setLoading(true)

    try {
      // /api/hello は Vite プロキシによって http://backend:8080/hello に転送される
      const response = await fetch('/api/hello')

      if (!response.ok) {
        throw new Error(`サーバーエラー: ${response.status}`)
      }

      // バックエンドは JSON ではなく文字列を返すので text() で読み取る
      const text = await response.text()
      setMessage(text)
    } catch (err) {
      // ネットワークエラーが発生した場合にわかりやすいエラーを表示する
      setError(err instanceof Error ? err.message : '不明なエラーが発生しました')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ fontFamily: 'sans-serif', padding: '2rem' }}>
      <h1>React + Spring Boot Demo</h1>

      {/* ボタンをクリックすると API を呼び出す */}
      <button onClick={handleFetch} disabled={loading}>
        {loading ? '読み込み中...' : '/hello API を呼び出す'}
      </button>

      {/* API のレスポンスを表示する */}
      {message && (
        <p style={{ color: 'green', marginTop: '1rem' }}>
          レスポンス: <strong>{message}</strong>
        </p>
      )}

      {/* エラーがあれば表示する */}
      {error && (
        <p style={{ color: 'red', marginTop: '1rem' }}>
          エラー: {error}
        </p>
      )}
    </div>
  )
}

export default App
