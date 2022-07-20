CREATE TABLE public."Users" (
	id int4 NOT NULL,
	login bpchar(32) NOT NULL,
	"fullName" bpchar(150) NOT NULL,
	email bpchar(320) NULL,
	"passwordHash" bpchar(80) NOT NULL,
	state bpchar(1) NOT NULL
);

CREATE SEQUENCE public.user_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;


alter table "Users" alter column id set default nextval('user_id_seq');

INSERT INTO public."Users"
(id, login, "fullName", email, "passwordHash", state)
VALUES(1, 'admin', 'admin', 'admin@gmail.ru', 'asdsdsaddadas', 'A');
