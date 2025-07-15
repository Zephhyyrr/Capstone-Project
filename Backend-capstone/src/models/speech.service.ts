import axios from 'axios';
import FormData from 'form-data';
import { Express } from 'express';
import fs from 'fs';
import path from 'path';

const FLASK_API_BASE_URL = 'http://127.0.0.1:5051';
const uploadPath = path.join(__dirname, "../../public/uploads"); 

export const speechService = {
    async speechToText(file: Express.Multer.File) {
        if (!file || !file.path) {
            throw new Error("File is missing or not in correct format");
        }

        const filePath = file.path;
        const fileName = path.basename(filePath);

        const formData = new FormData();
        formData.append('audio', fs.createReadStream(filePath), {
            filename: file.originalname,
            contentType: file.mimetype,
        });

        const response = await axios.post(`${FLASK_API_BASE_URL}/speech-to-text`, formData, {
            headers: formData.getHeaders(),
        });

        return {
            ...response.data,
            audioFileName: fileName,
            audioPath: `/uploads/${fileName}`,
        };
    },

    async analyzeSpeech(text: string, audioFileName?: string) {
        const formData = new FormData();
        formData.append('text', text);

        if (audioFileName) {
            const filePath = path.join(uploadPath, audioFileName);

            if (fs.existsSync(filePath)) {
                formData.append('audio', fs.createReadStream(filePath), {
                    filename: audioFileName,
                    contentType: 'audio/wav', 
                });
            }
        }

        const response = await axios.post(`${FLASK_API_BASE_URL}/analyze`, formData, {
            headers: formData.getHeaders(),
        });

        return response.data;
    }
};
