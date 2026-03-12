UPDATE question q SET DTYPE = 'ChoiceQuestion' WHERE DTYPE = 'BooleanQuestion';
UPDATE answer a SET DTYPE = 'ChoiceAnswer' WHERE DTYPE = 'BooleanAnswer';