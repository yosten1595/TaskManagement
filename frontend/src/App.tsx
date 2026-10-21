import { Link, Route, Routes } from 'react-router-dom'
import BoardPage from './pages/BoardPage'
import HomePage from './pages/HomePage'

export default function App() {
  return (
    <>
      <header className="app-header">
        <Link to="/">TaskBoard</Link>
      </header>
      <main>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/boards/:boardId" element={<BoardPage />} />
          <Route path="*" element={<p className="message">ページが見つかりません。</p>} />
        </Routes>
      </main>
    </>
  )
}
