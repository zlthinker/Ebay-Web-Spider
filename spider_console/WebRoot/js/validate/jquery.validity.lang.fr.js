$.extend($.validity.messages, {
    require:"#{field} est requis.",
    // Format validators:
    match:"#{field} est dans un mauvais format.",
    integer:"#{field} doit �tre un nombre entier positif.",
    date:"#{field} doit �tre une date.",
    email:"#{field} doit �tre une adresse email.",
    usd:"#{field} doit �tre un montant en US Dollars.",
    url:"#{field} doit �tre une adresse URL.",
    number:"#{field} doit �tre un nombre.",
    zip:"#{field} doit �tre un code postal ##### ou #####-####.",
    phone:"#{field} doit �tre un num�ro de t�l�phone ###-###-####.",
    guid:"#{field} doit �tre un guid du type {3F2504E0-4F89-11D3-9A0C-0305E82C3301}.",
    time24:"#{field} doit �tre une heure au format 24 heures (ex: 23:00).",
    time12:"#{field} doit �tre une heure au format 12 heures (ex:12:00 AM/PM)",

    // Value range messages:
    lessThan:"#{field} doit �tre inf�rieur � #{max}.",
    lessThanOrEqualTo:"#{field} doit �tre inf�rieur ou �gal � #{max}.",
    greaterThan:"#{field} doit �tre sup�rieur � #{min}.",
    greaterThanOrEqualTo:"#{field} doit �tre sup�rieur ou �gal � #{min}.",
    range:"#{field} doit �tre compris entre #{min} et #{max}.",

    // Value length messages:
    tooLong:"#{field} ne doit pas d�passer #{max} caract�res.",
    tooShort:"#{field} doit contenir au minimum #{min} caract�res.",

    // Composition validators:
    nonHtml:"#{field} ne doit pas contenir de caract�res HTML.",
    alphabet:"#{field} contient des carat�res interdits.",

    minCharClass:"#{field} ne doit pas contenir plus de #{min} #{charClass} caract�res.",
    maxCharClass:"#{field} ne doit pas contenir moins de #{min} #{charClass} caract�res.",
    
    // Aggregate validator messages:
    equal:"Les valeurs ne correspondent pas.",
    distinct:"Une valeur est r�p�t�e.",
    sum:"La somme des valeurs diff�re de #{sum}.",
    sumMax:"La somme des valeurs doit �tre inf�rieure � #{max}.",
    sumMin:"La somme des valeurs doit �tre sup�rieure � #{min}.",

    // Radio validator messages:
    radioChecked:"La valeur s�lectionn�e est invalide.",
    
    generic:"Invalide."
});

$.validity.setup({ defaultFieldName:"Ce champ", });