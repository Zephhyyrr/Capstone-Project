import express from 'express';
import { authenticateToken } from '../middleware/auth';
import { 
    getHistory, 
    getHistoryByIdController, 
    saveHistory, 
    deleteHistoryController 
} from '../controllers/history.controller';

const router = express.Router();

// GET /api/history - Get all history for user
router.get('/', authenticateToken, getHistory);

// POST /api/history/save - Save history
router.post('/save', authenticateToken, saveHistory);

// GET /api/history/:id - Get specific history by ID
router.get('/:id', authenticateToken, getHistoryByIdController);

// DELETE /api/history/:id - Delete history
router.delete('/:id', authenticateToken, deleteHistoryController);

export default router;
