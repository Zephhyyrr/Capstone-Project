import { Router } from 'express';
import { upload } from '../middleware/upload'
import { speechToText, analyzeSpeech } from './speech.controller'
import { authenticateToken } from '../middleware/auth';

const router = Router();

router.post('/speech-to-text', authenticateToken, upload.single('audio'), speechToText);

router.post('/analyze-speech', authenticateToken, analyzeSpeech);



export default router;