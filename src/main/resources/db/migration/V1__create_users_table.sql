-- V1__create_users_table.sql
     CREATE TABLE users (
       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
       username VARCHAR(100) NOT NULL UNIQUE,
       password VARCHAR(255) NOT NULL,
       phone VARCHAR(20),
       apartment VARCHAR(20),
       role VARCHAR(50),
       created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
       updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
     );
