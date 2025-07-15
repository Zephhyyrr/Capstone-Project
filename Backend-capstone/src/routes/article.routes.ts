import { Router } from "express";
import { createArticle, updateArticle, getArticles, getArticleById, deleteArticleById } from "../controllers/article.controller";

const router = Router();

router.post("/create", createArticle); 

router.put("/update/:id", updateArticle); 

router.get("/getAll", getArticles); 

router.get("/getArticle/:id", getArticleById); 

router.delete("/delete/:id", deleteArticleById);

export default router;