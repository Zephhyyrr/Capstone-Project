import { Request, Response, NextFunction } from "express";
import { create, update, get, getById, deleteArticle } from "../services/article.service";

const runMiddlewareChain = (
    middlewares: Array<(req: Request, res: Response, next: NextFunction) => any>
) => {
    return (req: Request, res: Response, next: NextFunction) => {
        let index = 0;

        const run = (err?: any) => {
            if (err) return next(err);
            if (index >= middlewares.length) return;

            const middleware = middlewares[index++];
            try {
                middleware(req, res, run);
            } catch (error) {
                next(error);
            }
        };

        run();
    };
};

export const createArticle = runMiddlewareChain(create);
export const updateArticle = runMiddlewareChain(update);
export const getArticles = runMiddlewareChain(get);
export const getArticleById = runMiddlewareChain(getById);
export const deleteArticleById = runMiddlewareChain(deleteArticle);
