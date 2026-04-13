import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'

// index.html の #root div に React アプリをマウントする
const rootElement = document.getElementById('root')!
createRoot(rootElement).render(
  <StrictMode>
    <App />
  </StrictMode>
)
