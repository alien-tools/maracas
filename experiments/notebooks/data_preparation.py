from datetime import datetime
import numpy as np
import pandas as pd


def compute_url_parts(url_col: str) -> tuple[str, str]:
    """
    Returns the GitHub scheme and authority of the clients data 
    frame column.
    :param url_col: name of the column in the data frame. Can 
        be 'sshUrl', 'url', 'csshUrl', or 'curl'
    :returns: tuple whose first element is the scheme of the
        expected urls (e.g. 'ssh', 'https') and its second 
        element is the authority of the expected urls.
    :raises: `ValueError` if the url_col is invalid.
    """
    if url_col in ['sshUrl', 'csshUrl']:
        return ('ssh', 'git@github.com')
    elif url_col in ['url', 'curl']:
        return ('https', 'github.com')
    else:
        raise Exception('Invalid url_col value')
        
        
def validate_urls(url_col: str, df: pd.DataFrame) -> bool:
    """
    Validates if the values of a given URL column within the clients
    data frame are valid. Concretely, it performs three checks:
    1. all URLs start with the expected scheme; 2. all URLs 
    contain the expected authority, and; 3. all URLs are built 
    in the right way and contain the expected path based on the 
    repository owner and name values.
    :param url_col: name of the column in the data frame. Can 
        be 'sshUrl', 'url', 'csshUrl', or 'curl'
    :param df: clients data frame
    :returns: `True` if all checks pass, `False` otherwise.
    """
    # Define owner and name column names
    owner_col = 'owner'
    name_col = 'name'
    if url_col.startswith('c'):
        owner_col = 'c' + owner_col
        name_col = 'c' + name_col
        
    # E.g., ssh://git@github.com:owner/name
    # or https://github.com/owner/name
    scheme, authority = compute_url_parts(url_col)
    delimiter = ':' if scheme == 'ssh' else '/'
    extension = '.git' if scheme == 'ssh' else ''
    
    # SSH URLs have the right scheme
    mask = df[url_col].str.match(f'^{scheme}://')
    scheme_check = mask.all()
    print(f'Records have right scheme: {scheme_check}')

    # SSH URLs have the right authority
    mask = df[url_col].str.contains(f'//{authority}{delimiter}')
    authority_check = mask.all()
    print(f'Records have right authority: {authority_check}')

    # SSH URLs have the right path
    mask = df[url_col] == f'{scheme}://{authority}{delimiter}' + df[owner_col].map(str) + '/' + df[name_col].map(str) + extension
    path_check = mask.all()
    print(f'Records have right path: {path_check}')
    
    return scheme_check and authority_check and path_check


def validate_date(date_col: str, df: pd.DataFrame) -> bool:
    """
    Validates if the values of a given date column within the 
    clients data frame are valid. Concretely, it checks if all 
    dates are of the format yyyy-MM-dd.
    :param date_col: name of the column in the data frame. Can 
        be 'createdAt' or 'pushedAt'
    :param df: clients data frame
    :returns: `True` if the check passes, `False` otherwise.
    :raises: `ValueError` if the date_col is invalid.
    """
    if date_col not in ['createdAt', 'pushedAt']:
        raise ValueError('Invalid date_col value')
        
    mask = df[date_col].str.match('^\d{4}-\d{2}-\d{2}')
    format_check = mask.all()
    print(f'Records have right format: {format_check}')
    
    return format_check


def validate_limit_date(limit_date: datetime, date_col: str, df: pd.DataFrame) -> bool:
    """
    Validates if the values of a given date column within the 
    clients data frame are valid. Concretely, it performs two 
    checks: 1. all date appear after a limit date, and; 2. all 
    dates appear before the present date.
    :param limit_date: limit past date for all records
    :param date_col: name of the column in the data frame. Can 
        be 'createdAt' or 'pushedAt'
    :param df: clients data frame
    :returns: `True` if all checks pass, `False` otherwise.
    :raises: `ValueError` if the date_col is invalid.
    """ 
    if date_col not in ['createdAt', 'pushedAt']:
        raise ValueError('Invalid date_col value')
    
    mask = pd.to_datetime(df[date_col]) > limit_date
    limit_check = mask.all()
    print(f'Records appear after {limit_date}: {limit_check}')
    
    present = datetime.now()
    mask = pd.to_datetime(df[date_col]) <= present
    present_check = mask.all()
    print(f'Records appear before {present}: {present_check}')
    
    return limit_check and present_check


def validate_limit_num(limit_num: int, num_col: str, df: pd.DataFrame) -> bool:
    """
    Validates if the values of a given numerical column within the 
    clients data frame are valid. Concretely, it checks if they are 
    greater than or equal to a limit number.
    :param limit_num: limit number
    :param num_col: name of the column in the data frame. Can 
        be 'stars', 'packages', 'clients', 'relevantClients' or 'cstars'
    :param df: clients data frame
    :returns: `True` if the check passes, `False` otherwise.
    :raises: `ValueError` if the num_col is invalid.
    """
    if num_col not in ['stars', 'packages', 'clients', 'relevantClients', 'cstars']:
        raise ValueError('Invalid num_colnum_col value')
    
    mask = df[num_col] >= limit_num
    limit_check = mask.all()
    print(f'Records have at least {limit_num}: {limit_check}')
    
    return limit_check