import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Points from './components/Points';
import Signup from './components/Signup';
import Login from './components/Login';
import Home from './components/Home';
import CreatePoint from './components/CreatePoint';

const App: React.FC = () => {
  return (
    <Router>
      <Routes>
        <Route path='/' element={<Home />}></Route>
        <Route path='/signup' element={<Signup />}></Route>
        <Route path='/login' element={<Login />}></Route>
        <Route path='/create' element={<CreatePoint />}></Route>
        <Route path='/points' element={<Points />}></Route>
      </Routes>
    </Router>
  )
}

export default App
