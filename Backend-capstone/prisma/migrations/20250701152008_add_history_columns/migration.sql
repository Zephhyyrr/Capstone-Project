/*
  Warnings:

  - You are about to drop the column `updateAt` on the `History` table. All the data in the column will be lost.
  - Added the required column `correctedParagraph` to the `History` table without a default value. This is not possible if the table is not empty.
  - Added the required column `originalParagraph` to the `History` table without a default value. This is not possible if the table is not empty.
  - Added the required column `updatedAt` to the `History` table without a default value. This is not possible if the table is not empty.

*/
-- AlterTable
ALTER TABLE "History" DROP COLUMN "updateAt",
ADD COLUMN     "correctedParagraph" TEXT NOT NULL,
ADD COLUMN     "originalParagraph" TEXT NOT NULL,
ADD COLUMN     "updatedAt" TIMESTAMP(3) NOT NULL;
